package org.javista.controller;

import org.javista.model.ITimeModel;
import org.javista.model.impl.GmtTimeModel;
import org.javista.model.impl.MstTimeModel;
import org.javista.model.impl.PstTimeModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TimeController {

 	@RequestMapping(value={"/time", "/home"}, method=RequestMethod.GET)
	public ModelAndView handleRequest(){
 		ITimeModel gmtModel = new GmtTimeModel();
 		ITimeModel mstModel = new MstTimeModel();
 		ITimeModel pstModel = new PstTimeModel();
		ModelAndView mv = new ModelAndView("timekeeper");
		mv.addObject("gmtmodel", gmtModel);
		mv.addObject("mstmodel", mstModel);
		mv.addObject("pstmodel", pstModel);
		mv.addObject("istmodel", istModel);
		return mv;
	}
}
