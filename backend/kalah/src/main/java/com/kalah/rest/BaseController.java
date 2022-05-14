package com.kalah.rest;

import com.kalah.general.Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class BaseController {

	public static final String MAIN_PAGE = Utils.loadResourceAsString(new File("main-page.html"));

	@GetMapping("/")
	public String index() {
		return MAIN_PAGE;
	}

}