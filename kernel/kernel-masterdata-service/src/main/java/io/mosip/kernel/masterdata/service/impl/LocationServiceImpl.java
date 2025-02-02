package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.LocationErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.LocationDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.StatusResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.LocationExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.PostLocationCodeResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.service.LocationService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * Class will fetch Location details based on various parameters this class is
 * implemented from {@link LocationService}}
 * 
 * @author Srinivasan
 * @author Tapaswini
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@Service
public class LocationServiceImpl implements LocationService {

	/**
	 * creates an instance of repository class {@link LocationRepository}}
	 */
	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private FilterTypeValidator filterTypeValidator;

	@Autowired
	private MasterdataSearchHelper masterdataSearchHelper;

	@Autowired
	FilterColumnValidator filterColumnValidator;

	@Autowired
	MasterDataFilterHelper masterDataFilterHelper;

	private List<Location> childHierarchyList = null;
	private List<Location> hierarchyChildList = null;
	private List<Location> parentHierarchyList = null;
	private List<String> childList = null;

	/**
	 * This method will all location details from the Database. Refers to
	 * {@link LocationRepository} for fetching location hierarchy
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#getLocationDetails(java.
	 * lang.String)
	 */
	@Override
	public LocationHierarchyResponseDto getLocationDetails(String langCode) {
		List<LocationHierarchyDto> responseList = null;
		LocationHierarchyResponseDto locationHierarchyResponseDto = new LocationHierarchyResponseDto();
		List<Object[]> locations = null;
		try {

			locations = locationRepository.findDistinctLocationHierarchyByIsDeletedFalse(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (!locations.isEmpty()) {

			responseList = MapperUtils.objectToDtoConverter(locations);

		} else {
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		locationHierarchyResponseDto.setLocations(responseList);
		return locationHierarchyResponseDto;
	}

	/**
	 * This method will fetch location hierarchy based on location code and language
	 * code Refers to {@link LocationRepository} for fetching location hierarchy
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return LocationHierarchyResponseDto-
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#
	 * getLocationHierarchyByLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public LocationResponseDto getLocationHierarchyByLangCode(String locCode, String langCode) {
		List<Location> childList = null;
		List<Location> parentList = null;
		childHierarchyList = new ArrayList<>();
		parentHierarchyList = new ArrayList<>();
		LocationResponseDto locationHierarchyResponseDto = new LocationResponseDto();
		try {

			List<Location> locHierList = getLocationHierarchyList(locCode, langCode);
			if (locHierList != null && !locHierList.isEmpty()) {
				for (Location locationHierarchy : locHierList) {
					String currentParentLocCode = locationHierarchy.getParentLocCode();
					childList = getChildList(locCode, langCode);
					parentList = getParentList(currentParentLocCode, langCode);

				}
				locHierList.addAll(childList);
				locHierList.addAll(parentList);

				List<LocationDto> locationHierarchies = MapperUtils.mapAll(locHierList, LocationDto.class);
				locationHierarchyResponseDto.setLocations(locationHierarchies);

			} else {
				throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		}

		catch (DataAccessException | DataAccessLayerException e) {

			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));

		}
		return locationHierarchyResponseDto;
	}

	/**
	 * Method creates location hierarchy data into the table based on the request
	 * parameter sent {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#createLocationHierarchy(io
	 * .mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	@Transactional
	public PostLocationCodeResponseDto createLocationHierarchy(LocationDto locationRequestDto) {

		Location location = null;
		Location locationResultantEntity = null;
		PostLocationCodeResponseDto locationCodeDto = null;

		location = MetaDataUtils.setCreateMetaData(locationRequestDto, Location.class);
		try {
			locationResultantEntity = locationRepository.create(location);
		} catch (DataAccessLayerException | DataAccessException ex) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_INSERT_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_INSERT_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(ex));
		}

		locationCodeDto = MapperUtils.map(locationResultantEntity, PostLocationCodeResponseDto.class);
		return locationCodeDto;
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#updateLocationDetails(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	@Transactional
	public PostLocationCodeResponseDto updateLocationDetails(LocationDto locationDto) {
		PostLocationCodeResponseDto postLocationCodeResponseDto = new PostLocationCodeResponseDto();
		CodeAndLanguageCodeID locationId = new CodeAndLanguageCodeID();
		locationId.setCode(locationDto.getCode());
		locationId.setLangCode(locationDto.getLangCode());
		try {
			Location location = locationRepository.findById(Location.class, locationId);

			if (location == null) {
				throw new RequestException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			if (!locationDto.getIsActive() && findIsActiveInHierarchy(location)) {
				throw new RequestException(LocationErrorCode.LOCATION_CHILD_STATUS_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_CHILD_STATUS_EXCEPTION.getErrorMessage());
			}
			location = MetaDataUtils.setUpdateMetaData(locationDto, location, true);
			locationRepository.update(location);
			MapperUtils.map(location, postLocationCodeResponseDto);

		} catch (DataAccessException | DataAccessLayerException ex) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(ex));
		}

		return postLocationCodeResponseDto;
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#deleteLocationDetials(java
	 * .lang.String)
	 */
	@Override
	@Transactional
	public CodeResponseDto deleteLocationDetials(String locationCode) {
		List<Location> locations = null;
		CodeResponseDto codeResponseDto = new CodeResponseDto();
		try {
			locations = locationRepository.findByCode(locationCode);
			if (!locations.isEmpty()) {

				locations.stream().map(MetaDataUtils::setDeleteMetaData)
						.forEach(location -> locationRepository.update(location));

			} else {
				throw new RequestException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessException | DataAccessLayerException ex) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(ex));
		}
		codeResponseDto.setCode(locationCode);
		return codeResponseDto;
	}

	/**
	 * Method creates location hierarchy data into the table based on the request
	 * parameter sent {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#
	 * getLocationDataByHierarchyName(java.lang.String)
	 */
	@Override
	public LocationResponseDto getLocationDataByHierarchyName(String hierarchyName) {
		List<Location> locationlist = null;
		LocationResponseDto locationHierarchyResponseDto = new LocationResponseDto();
		try {
			locationlist = locationRepository.findAllByHierarchyNameIgnoreCase(hierarchyName);

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}

		if (!(locationlist.isEmpty())) {
			List<LocationDto> hierarchyList = MapperUtils.mapAll(locationlist, LocationDto.class);
			locationHierarchyResponseDto.setLocations(hierarchyList);

		} else {

			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		return locationHierarchyResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#
	 * getImmediateChildrenByLocCodeAndLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public LocationResponseDto getImmediateChildrenByLocCodeAndLangCode(String locCode, String langCode) {
		List<Location> locationlist = null;
		LocationResponseDto locationHierarchyResponseDto = new LocationResponseDto();
		try {
			locationlist = locationRepository.findLocationHierarchyByParentLocCodeAndLanguageCode(locCode, langCode);

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}

		if (locationlist.isEmpty()) {
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		List<LocationDto> locationDtoList = MapperUtils.mapAll(locationlist, LocationDto.class);
		locationHierarchyResponseDto.setLocations(locationDtoList);
		return locationHierarchyResponseDto;
	}

	/**
	 * fetches location hierarchy details from database based on location code and
	 * language code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarchy>
	 */
	private List<Location> getLocationHierarchyList(String locCode, String langCode) {

		return locationRepository.findLocationHierarchyByCodeAndLanguageCode(locCode, langCode);
	}

	/**
	 * fetches location hierarchy details from database based on parent location
	 * code and language code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarchy>
	 */
	private List<Location> getLocationChildHierarchyList(String locCode, String langCode) {

		return locationRepository.findLocationHierarchyByParentLocCodeAndLanguageCode(locCode, langCode);

	}

	/**
	 * This method fetches child hierarchy details of the location based on location
	 * code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<Location>
	 */
	private List<Location> getChildList(String locCode, String langCode) {

		if (locCode != null && !locCode.isEmpty()) {
			List<Location> childLocHierList = getLocationChildHierarchyList(locCode, langCode);
			childHierarchyList.addAll(childLocHierList);
			childLocHierList.parallelStream().filter(entity -> entity.getCode() != null && !entity.getCode().isEmpty())
					.map(entity -> getChildList(entity.getCode(), langCode)).collect(Collectors.toList());
		}

		return childHierarchyList;
	}

	/**
	 * This method fetches parent hierarchy details of the location based on parent
	 * Location code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarcy>
	 */
	private List<Location> getParentList(String locCode, String langCode) {

		if (locCode != null && !locCode.isEmpty()) {
			List<Location> parentLocHierList = getLocationHierarchyList(locCode, langCode);
			parentHierarchyList.addAll(parentLocHierList);

			parentLocHierList.parallelStream()
					.filter(entity -> entity.getParentLocCode() != null && !entity.getParentLocCode().isEmpty())
					.map(entity -> getParentList(entity.getParentLocCode(), langCode)).collect(Collectors.toList());
		}

		return parentHierarchyList;
	}

	@Override
	public Map<Short, List<Location>> getLocationByLangCodeAndHierarchyLevel(String langCode, Short hierarchyLevel) {
		Map<Short, List<Location>> map = new TreeMap<>();
		List<Location> locations = locationRepository.getAllLocationsByLangCodeAndLevel(langCode, hierarchyLevel);
		if (!EmptyCheckUtils.isNullEmpty(locations)) {
			for (Location location : locations) {
				if (map.containsKey(location.getHierarchyLevel())) {
					map.get(location.getHierarchyLevel()).add(location);
				} else {
					List<Location> list = new ArrayList<>();
					list.add(location);
					map.put(location.getHierarchyLevel(), list);
				}
			}
			return map;
		} else {
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#validateLocationName(java.
	 * lang.String)
	 */
	@Override
	public StatusResponseDto validateLocationName(String locationName) {
		StatusResponseDto statusResponseDto = null;
		boolean isPresent = false;
		try {
			statusResponseDto = new StatusResponseDto();
			statusResponseDto.setStatus(MasterDataConstant.INVALID);
			isPresent = locationRepository.isLocationNamePresent(locationName);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage());
		}
		if (isPresent) {
			statusResponseDto.setStatus(MasterDataConstant.VALID);
		}
		return statusResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#getLocations(int,
	 * int, java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<LocationExtnDto> getLocations(int pageNumber, int pageSize, String sortBy, String orderBy) {
		List<LocationExtnDto> locations = null;
		PageDto<LocationExtnDto> pageDto = null;
		try {
			Page<Location> pageData = locationRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				locations = MapperUtils.mapAll(pageData.getContent(), LocationExtnDto.class);
				pageDto = new PageDto<>(pageData.getNumber(), pageData.getTotalPages(), pageData.getTotalElements(),
						locations);
			} else {
				throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage());
		}
		return pageDto;
	}

	/**
	 * This method to find, is there any child of given Location isActive is true
	 * then return true and break the loop. Otherwise if all children of the given
	 * location are false the return false.
	 * 
	 * @param location
	 * @return boolean return true or false
	 */
	public boolean findIsActiveInHierarchy(Location location) {
		boolean flag = false;
		String locCode = location.getCode();
		String langCode = location.getLangCode();

		List<Location> childList = new ArrayList<>();
		hierarchyChildList = new ArrayList<>();
		childList = getIsActiveChildList(locCode, langCode);
		if (!childList.isEmpty())
			return true;
		return flag;
	}

	/**
	 * This method fetches child hierarchy details of the location based on location
	 * code, here child isActive can true or false
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<Location>
	 */
	private List<Location> getIsActiveChildList(String locCode, String langCode) {

		if (locCode != null && !locCode.isEmpty()) {
			List<Location> childLocHierList = getIsActiveLocationChildHierarchyList(locCode, langCode);
			hierarchyChildList.addAll(childLocHierList);
			childLocHierList.parallelStream().filter(entity -> entity.getCode() != null && !entity.getCode().isEmpty())
					.map(entity -> getIsActiveChildList(entity.getCode(), langCode)).collect(Collectors.toList());
		}
		return hierarchyChildList;

	}

	/**
	 * fetches location hierarchy details from database based on parent location
	 * code and language code, children's isActive is either true or false
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarchy>
	 */
	private List<Location> getIsActiveLocationChildHierarchyList(String locCode, String langCode) {

		return locationRepository.findDistinctByparentLocCode(locCode, langCode);

	}

	/**
	 * This method fetches child hierarchy details of the location based on location
	 * code, here child isActive can true or false
	 * 
	 * @param locCode
	 *            - location code
	 * @return List<Location>
	 */
	@Override
	public List<String> getChildList(String locCode) {
		childList = new ArrayList<>();
		List<String> resultList = new ArrayList<>();
		resultList = getChildByLocCode(locCode);
		if (!resultList.isEmpty())
			return resultList;
		return Arrays.asList(locCode);
	}

	private List<String> getChildByLocCode(String locCode) {
		if (locCode != null && !locCode.isEmpty()) {
			List<String> childLocHierList = getLocationChildHierarchyList(locCode);
			childList.addAll(childLocHierList);
			childLocHierList.parallelStream().filter(Objects::nonNull).map(this::getChildByLocCode)
					.collect(Collectors.toList());
		}
		return childList;
	}

	/**
	 * fetches location hierarchy details from database based on parent location
	 * code and language code, children's isActive is either true or false
	 * 
	 * @param locCode
	 *            - location code
	 * @return List<LocationHierarchy>
	 */
	private List<String> getLocationChildHierarchyList(String locCode) {

		return locationRepository.findDistinctByparentLocCode(locCode);

	}

	/* (non-Javadoc)
	 * @see io.mosip.kernel.masterdata.service.LocationService#searchLocation(io.mosip.kernel.masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<LocationExtnDto> searchLocation(SearchDto dto) {
		PageResponseDto<LocationExtnDto> pageDto = new PageResponseDto<>();
		List<LocationExtnDto> location = null;
		if (filterTypeValidator.validate(LocationExtnDto.class, dto.getFilters())) {
			Page<Location> page = masterdataSearchHelper.searchMasterdata(Location.class, dto, null);
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				pageDto = PageUtils.pageResponse(page);
				location = MapperUtils.mapAll(page.getContent(), LocationExtnDto.class);
				pageDto.setData(location);
			}
		}
		return pageDto;
	}

	/* (non-Javadoc)
	 * @see io.mosip.kernel.masterdata.service.LocationService#locationFilterValues(io.mosip.kernel.masterdata.dto.request.FilterValueDto)
	 */
	@Override
	public FilterResponseDto locationFilterValues(FilterValueDto filterValueDto) {
		FilterResponseDto filterResponseDto = new FilterResponseDto();
		List<ColumnValue> columnValueList = new ArrayList<>();
		List<String> getValues = null;
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters())) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				if (filterDto.getType().equals("unique")) {
					getValues = locationRepository.filterByDistinctHierarchyLevel(
							Integer.parseInt(filterDto.getColumnName()), filterValueDto.getLanguageCode());
				} else {
					getValues = locationRepository.filterByHierarchyLevel(Integer.parseInt(filterDto.getColumnName()),
							filterValueDto.getLanguageCode());
				}

				getValues.forEach(value -> {
					ColumnValue columnValue = new ColumnValue();
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(value.toString());
					columnValueList.add(columnValue);

				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}

}
