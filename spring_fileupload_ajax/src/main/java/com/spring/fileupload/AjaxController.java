package com.spring.fileupload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.spring.domain.AttachVO;

import net.coobird.thumbnailator.Thumbnailator;

@Controller
public class AjaxController {
	private static final Logger logger=
			LoggerFactory.getLogger(AjaxController.class);
	
	//uploadAjax.jsp 보여주는 컨트롤러 작성
	@GetMapping("/uploadAjax")
	public void uploadAjax() {
		logger.info("uploadAjax 폼 요청");
	}
	
	@PostMapping(value="/uploadAjax",produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<List<AttachVO>> uploadAjaxPost(MultipartFile[] uploadFile) {
		logger.info("파일업로드 요청");
		
		String uploadPath="d:\\upload";
		String uploadFileName=null;
		
		List<AttachVO> list=new ArrayList<>();
		
		for(MultipartFile file : uploadFile) {
			logger.info("originalName "+file.getOriginalFilename());
			logger.info("size "+file.getSize());
			logger.info("contentType "+file.getContentType());
						
			String originalFileName=file.getOriginalFilename();
			
			//파일명 앞에 uuid 값 붙이기
			UUID uuid=UUID.randomUUID();			
			uploadFileName=uuid.toString()+"_"+originalFileName;	
			
			AttachVO vo=new AttachVO();
			vo.setUuid(uuid.toString());
			vo.setFileName(originalFileName); //원본 파일명 담기
			vo.setUploadPath(uploadPath);
			
			
			File saveFile=new File(uploadPath,uploadFileName);
			
			try {
				
				
				if(checkImageType(saveFile)) {
					vo.setFileType(true);
					//이미지가 맞다면 썸네일 작업 해주기
					File f=new File(uploadPath,"s_"+uploadFileName);
					FileOutputStream thumbnail=new FileOutputStream(f);
					Thumbnailator.createThumbnail(file.getInputStream(),
							thumbnail, 100, 100);
					thumbnail.close();
				}
				file.transferTo(saveFile);
				list.add(vo);
				
			} catch (IOException e) {			
				e.printStackTrace();
			}		
		}
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	
	@GetMapping(value="/download",produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(String fileName,@RequestHeader("User-Agent")String userAgent){
		logger.info("파일명  "+fileName);
		
		Resource resource=new FileSystemResource("d:\\upload\\"+fileName);
		logger.info(resource+"  ");
		
		//uuid값 포함 상태
		String resourceName=resource.getFilename();
		//uuid값 제거
		String oriName=
				resourceName.substring(resourceName.indexOf("_")+1);
		
		HttpHeaders headers = new HttpHeaders();
		String downloadName=null;
		
		try {
			
			if(userAgent.contains("Trident")) {
				logger.info("익스플로러 11");
				downloadName=URLEncoder.encode(oriName,"UTF-8").replaceAll("\\+", " ");
			}else if(userAgent.contains("Edge")) {
				logger.info("Edge");
				downloadName=URLEncoder.encode(oriName,"UTF-8");
			}else {
				logger.info("Chrome..");
				downloadName=new String(oriName.getBytes("utf-8"),"ISO-8859-1");
			}
			
			
			headers.add("Content-Disposition","attachment;filename="+downloadName);
				
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
		}
		
		return new ResponseEntity<Resource>(resource,headers,HttpStatus.OK);
	}	
	
	@GetMapping("/display")
	@ResponseBody
	public ResponseEntity<byte[]> getFile(String fileName){
		logger.info("썸네일 보여주기 "+fileName);
		
		File file=new File(fileName);		
		
		ResponseEntity<byte[]> result=null;				
		try {
				
			HttpHeaders header=new HttpHeaders();
			header.add("Content-Type", 
					Files.probeContentType(file.toPath()));
			result=new ResponseEntity<byte[]>(
					FileCopyUtils.copyToByteArray(file),HttpStatus.OK);			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return result;
	}
	
	@PostMapping("/deleteFile")
	public ResponseEntity<String> deleteFile(String fileName,String type){
		
		logger.info("파일 삭제...."+fileName);
		
		File file=null;
		
		try {
			file=new File(URLDecoder.decode(fileName, "utf-8"));
			file.delete(); //파일 삭제(이미지인 경우 섬네일 삭제)
			
			if(type.equals("image")) {
				String oriName=file.getAbsolutePath().replace("s_", "");
				file=new File(oriName);
				file.delete(); //이미지 원본 파일 삭제
			}			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<String>("deleted",HttpStatus.OK);
	}
	
	
	private boolean checkImageType(File file) {
		//image파일 여부 확인
		try {
			String contentType=Files.probeContentType(file.toPath());
			return contentType.startsWith("image");
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return false;
	}
	
}





