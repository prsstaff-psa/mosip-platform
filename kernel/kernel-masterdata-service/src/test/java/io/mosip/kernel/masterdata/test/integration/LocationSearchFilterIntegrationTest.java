package io.mosip.kernel.masterdata.test.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.masterdata.dto.getresponse.extn.LocationExtnDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.repository.MachineRepository;
import io.mosip.kernel.masterdata.test.TestBootApplication;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class LocationSearchFilterIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FilterTypeValidator filterTypeValidator;

	@MockBean
	private MasterdataSearchHelper masterdataSearchHelper;

	@MockBean
	private MasterDataFilterHelper masterDataFilterHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private MachineRepository machineRepository;

	private SearchSort sort;
	private SearchDto searchDto;

	private RequestWrapper<SearchDto> request;

	@Before
	public void setup() throws JsonProcessingException {

		request = new RequestWrapper<>();
		searchDto = new SearchDto();
		Pagination pagination = new Pagination(0, 10);
		searchDto.setLanguageCode("eng");
		searchDto.setPagination(pagination);
		searchDto.setSort(Arrays.asList(sort));
		request.setRequest(searchDto);

		when(filterTypeValidator.validate(ArgumentMatchers.<Class<LocationExtnDto>>any(), Mockito.anyList()))
				.thenReturn(true);

	}

	@Test
	@WithUserDetails("test")
	public void searchLocationTest() throws Exception {
		SearchFilter searchFilter = new SearchFilter();
		searchFilter.setColumnName("hierarchyLevel");
		searchFilter.setType("equals");
		searchFilter.setValue("0");
		SearchDto searchDto = new SearchDto();
		searchDto.setFilters(Arrays.asList(searchFilter));
		searchDto.setLanguageCode("eng");
		Pagination pagination = new Pagination();
		pagination.setPageFetch(5);
		pagination.setPageStart(0);
		searchDto.setPagination(pagination);
		searchDto.setSort(Arrays.asList());
		request.setRequest(searchDto);
		String json = objectMapper.writeValueAsString(request);
		Location location = new Location();
		location.setHierarchyLevel((short) 0);
		Page<Location> pageContentData = new PageImpl<>(Arrays.asList(location));
		when(masterdataSearchHelper.searchMasterdata(Mockito.eq(Location.class), Mockito.any(), Mockito.any()))
				.thenReturn(pageContentData);
		mockMvc.perform(post("/locations/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("test")
	public void filterLocationTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("0");
		filterDto.setType("all");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		when(masterDataFilterHelper.filterValues(Mockito.eq(Location.class), Mockito.any(), Mockito.any()))
				.thenReturn(Arrays.asList());
		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

}
