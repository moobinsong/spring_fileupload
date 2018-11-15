package com.spring.fileupload;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

@Controller
public class FileUploadController {
	private static final Logger logger=
			LoggerFactory.getLogger(FileUploadController.class);
	
	//servlet-context.xml에 설정된 값(파일저장경로) 가져오기
	@javax.annotation.Resource(name="uploadPath")
	private String uploadPath;
	
	@GetMapping("/uploadForm")
	public void uploadForm() {
		logger.info("uploadForm 요청......");
	}
	
	@PostMapping("/uploadFormAction")
	public void uploadFormResult(MultipartFile[] uploadFile) {
		logger.info("upload 요청......");
		
		
		for(MultipartFile file : uploadFile) {
			logger.info("originalName "+file.getOriginalFilename());
			logger.info("size "+file.getSize());
			logger.info("contentType "+file.getContentType());
			
			File saveFile=new File(uploadPath,file.getOriginalFilename());
			
			try {
				file.transferTo(saveFile);				
			} catch (IOException e) {			
				e.printStackTrace();
			}		
		}		
	}
	
	/*@GetMapping(value="/download",produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(String fileName,@RequestHeader("User-Agent")String userAgent){
		logger.info("파일명  "+fileName);
		
		Resource resource=new FileSystemResource("d:\\upload\\"+fileName);
		logger.info(resource+"  ");
		
		String resourceName=resource.getFilename();
		HttpHeaders headers=new HttpHeaders();
		String downloadName=null;
		try {
			if(userAgent.contains("Trident")) {
				logger.info("익스플로러 11");
				downloadName=URLEncoder.encode(resourceName,"UTF-8").replaceAll("\\+", " ");
			}else if(userAgent.contains("Edge")) {
				logger.info("Edge");
				downloadName=URLEncoder.encode(resourceName,"UTF-8");
			}else {
				logger.info("Chrome..");
				downloadName=new String(resourceName.getBytes("utf-8"),"ISO-8859-1");
			}
			
			
			headers.add("Content-Disposition","attachment;filename="+downloadName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Resource>(resource,headers,HttpStatus.OK);
	}*/
	
	
}




