<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style>
.uploadResult {
	width: 100%;
	background-color: gray;
}

.uploadResult ul {
	display: flex;
	flex-flow: row;
	justify-content: center;
	align-items: center;
}

.uploadResult ul li {
	list-style: none;
	padding: 10px;
}

.uploadResult ul li img {
	width: 100px;
}
.bigPictureWrapper {
  position: absolute;
  display: none;
  justify-content: center;
  align-items: center;
  top:0%;
  width:100%;
  height:100%;
  background-color: gray; 
  z-index: 100;
}

.bigPicture {
  position: relative;
  display:flex;
  justify-content: center;
  align-items: center;
}
</style>
</head>
<body>
	<h1>Upload with Ajax</h1>
		<div class='bigPictureWrapper'>
		  <div class='bigPicture'>
		  </div>
		</div>	
	
		<div class='uploadDiv'>
			<input type='file' name='uploadFile' multiple>
		</div>
	
		<div class='uploadResult'>
			<ul>
	
			</ul>
		</div>
		<button id='uploadBtn'>Upload</button>

		<script src="https://code.jquery.com/jquery-3.3.1.min.js"
			integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
			crossorigin="anonymous"></script>
			
		<script>
			$(function(){
				var cloneObj=$(".uploadDiv").clone();
				
				
				$("#uploadBtn").click(function(){
					//Upload 버튼을 클릭하면 사용자가 선택한 파일들을
					//Ajax 기술을 사용하여 서버에 업로드 시키기
					console.log("업로드 호출");
					//form안에 들어있는 데이터 담을 변수 선언
					var formData=new FormData();
					
					//현재 첨부된 파일명들 배열로 가져오기
					var inputFile=$("input[name='uploadFile']");
					var files = inputFile[0].files;	
					console.log(files);
					//사용자가 첨부한 파일들 중에서 확장자 체크와
					//파일 사이즈 체크
					for(var i=0;i<files.length;i++){
						if(!checkExtension(files[i].name,files[i].size)){
							return false;
						}
						formData.append("uploadFile",files[i]);
					}
					
					//ajax 사용하여 서버로 데이터 보내기
					$.ajax({
						url : '/uploadAjax',
						data : formData,
						processData : false,
						contentType : false,
						type : 'post',
						dataType : 'json',
						success:function(result){
							console.log(result);
							showUploadedResult(result);
							$(".uploadDiv").html(cloneObj.html());
						}
					});
				});
				
				function showUploadedResult(uploadResultArr){
					//첨부된 파일 보여주기
					var str="";
					var uploadResult=$(".uploadResult ul");
					
					$(uploadResultArr).each(function(i,obj){
						if(obj.fileType){//image파일인경우
							var fileCallPath=encodeURIComponent(obj.uploadPath+"/s_"
									+obj.uuid+"_"+obj.fileName);
						
							var oriPath=obj.uploadPath+"/"+obj.uuid+"_"+obj.fileName;
							oriPath=oriPath.replace(new RegExp(/\\/g),"/");
						
							str+="<li><a href=\"javascript:showImage(\'"+oriPath+"\')\">";
							str+="<img src='/display?fileName="+fileCallPath+"'>";
							str+=obj.fileName+"</a>";
							str+="<span data-file='"+fileCallPath+"' data-type='image'> x </span></li>";	
						}else{//image 파일이 아닌 경우
							var fileCallPath=encodeURIComponent(obj.uploadPath+"/"
									+obj.uuid+"_"+obj.fileName);
							str+="<li><a href='/download?fileName="+fileCallPath;
							str+="'> <img src='/resources/img/attach.png'>";
							str+=obj.fileName+"</a>";
							str+="<span data-file='"+fileCallPath+"' data-type='file'> x </span></li>";
						}//else 종료
					});//each 종료
					console.log(str);
					uploadResult.append(str);					
				}//showUploadedResult 종료			
				
				function checkExtension(fileName,fileSize){
					//자바스크립트 정규식 표현을 사용하여 
					//파일 업로드가 가능한 파일인지 확인
					var regex = new RegExp("(.*?)\.(exe|sh|zip|alz)$");
					var maxSize = 5242880; //5MB
					if(fileSize>maxSize){
						alert("파일 사이즈 초과");
						return false;
					}
					if(regex.test(fileName)){
						alert("해당 종류의 파일은 업로드 할 수 없습니다.");
						return false;
					}
					return true;
				}//	checkExtension 종료	
				
				$(".bigPictureWrapper").click(function(){
					//원본사진 닫기
					$(".bigPicture").animate({width:'0%',height:'0%'},1000);
					setTimeout(function(){
						$(".bigPictureWrapper").hide();
					},1000);
				});
				
				$(".uploadResult").on("click","span",function(){
					var targetFile=$(this).data("file");
					var type=$(this).data("type");
					var targetLi=$(this).closest("li");
					
					$.ajax({
						url:'/deleteFile',
						dataType:'text',
						data:{
							fileName:targetFile,
							type:type
						},
						type:'post',
						success:function(result){
							console.log(result);
							targetLi.remove();
						}
					});
					
				});
				
			});
		
			function showImage(fileCallPath){
				//썸네일 이미지를 클릭하면 화면에 크게 보여주기
				$(".bigPictureWrapper").css("display","flex").show();
				
				$(".bigPicture").html("<img src='/display?fileName="+fileCallPath+"'>")
						.animate({width:'100%',height:'100%'},1000);
			}			
			
		</script>	

</body>
</html>
