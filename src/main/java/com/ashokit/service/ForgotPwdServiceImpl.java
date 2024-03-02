package com.ashokit.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ashokit.bindings.User;
import com.ashokit.constants.AppConstants;
import com.ashokit.entities.UserEntity;
import com.ashokit.props.AppProperties;
import com.ashokit.repositories.UserRepository;
import com.ashokit.util.EmailUtils;

@Service
public class ForgotPwdServiceImpl implements ForgotPwdService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private EmailUtils emailUtils;

	@Autowired
	private AppProperties props;

	@Override
	public String forgotPwd(String email) {
		UserEntity user = userRepo.findByUserEmail(email);
		if (user == null) {
			return props.getMessages().get(AppConstants.NO_RECORD);
		}
         if  (sendForgotPwdEmail(user)) {
			return props.getMessages().get(AppConstants.PWD_SENT);
		}
		return props.getMessages().get(AppConstants.ERROR);
	}

	public boolean sendForgotPwdEmail(UserEntity user) {
		String to = user.getUserEmail();
        String subject = props.getMessages().get(AppConstants.FORGOT_PWD_MAIL_SUBJECT);
        String bodyfilename = props.getMessages().get(AppConstants.FORGOT_PWD_MAIL_BODY_TEMPLATE_FILE);
        String body = readMailBody(bodyfilename, user);
       return  emailUtils.sendEmail(subject, body, to);
	}
	public String readMailBody (String fileName , UserEntity user) {
		String mailBody = null;
		StringBuffer buffer = new StringBuffer();
		Path path = Paths.get(fileName);
		try (Stream<String> stream = Files.lines(path)) { 
			stream.forEach(line -> {
				buffer.append(line);
			});
			mailBody = buffer.toString();
			mailBody = mailBody.replace(AppConstants.FNAME, user.getUserFname());
			mailBody = mailBody.replace(AppConstants.PWD, user.getUserPwd());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return mailBody;
	}
}
