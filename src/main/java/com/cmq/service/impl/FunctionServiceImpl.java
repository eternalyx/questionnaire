package com.cmq.service.impl;

import com.cmq.bo.response.FunctionTreeBO;
import com.cmq.mapper.FunctionMapper;
import com.cmq.po.FunctionPO;
import com.cmq.service.FunctionService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("functionService")
public class FunctionServiceImpl implements FunctionService {

    @Resource
    private FunctionMapper functionMapper;

    @Override
    public FunctionTreeBO findAllFunctionsAsTree() {
        List<FunctionPO> allPOs = functionMapper.findAll();

        FunctionTreeBO rootBO = new FunctionTreeBO();

        FunctionPO rootPO = allPOs.stream().filter(function -> function.getParentId() == -1).findFirst().get();

        BeanUtils.copyProperties(rootPO, rootBO);

        List<FunctionTreeBO> curFatherBOs = Arrays.asList(rootBO);

        List<Integer> fatherIds = Arrays.asList(rootBO.getId());

        List<FunctionPO> curChildrenPOs = allPOs.stream().filter(function -> fatherIds.contains(function.getParentId())).collect(Collectors.toList());

        while (!CollectionUtils.isEmpty(curChildrenPOs)){

            List<FunctionTreeBO> nextFatherBOs = new ArrayList<>();

            for(FunctionPO childrenPO : curChildrenPOs){
                for(FunctionTreeBO fatherBO : curFatherBOs){
                    if(fatherBO.getId().equals(childrenPO.getParentId())){
                        FunctionTreeBO childrenBO = new FunctionTreeBO();

                        BeanUtils.copyProperties(childrenPO, childrenBO);

                        fatherBO.getChildren().add(childrenBO);
                        nextFatherBOs.add(childrenBO);
                    }
                }
            }

            //next loop
            curFatherBOs = nextFatherBOs;

            List<Integer> curFatherIds = curFatherBOs.stream().map(FunctionTreeBO::getId).collect(Collectors.toList());

            curChildrenPOs = allPOs.stream().filter(function -> curFatherIds.contains(function.getParentId())).collect(Collectors.toList());
        }
        return rootBO;
    }
}