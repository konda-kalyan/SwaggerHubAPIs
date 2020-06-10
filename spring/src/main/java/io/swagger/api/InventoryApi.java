/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.13).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package io.swagger.api;

import io.swagger.model.InventoryItem;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-06-10T16:19:08.913Z")

@Api(value = "inventory", description = "the inventory API")
@RequestMapping(value = "/konda.kalyan/simple_emp_api2/1.0.0")
public interface InventoryApi {

    @ApiOperation(value = "adds an inventory item", nickname = "addInventory", notes = "Adds an item to the system", tags={ "admins", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "item created"),
        @ApiResponse(code = 400, message = "invalid input, object invalid"),
        @ApiResponse(code = 409, message = "an existing item already exists") })
    @RequestMapping(value = "/inventory",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<Void> addInventory(@ApiParam(value = "Inventory item to add"  )  @Valid @RequestBody InventoryItem inventoryItem) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default InventoryApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "searches inventory", nickname = "searchInventory", notes = "By passing in the appropriate options, you can search for available inventory in the system ", response = InventoryItem.class, responseContainer = "List", tags={ "developers", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "search results matching criteria", response = InventoryItem.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "bad input parameter") })
    @RequestMapping(value = "/inventory",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<List<InventoryItem>> searchInventory(@ApiParam(value = "pass an optional search string for looking up inventory") @Valid @RequestParam(value = "searchString", required = false) String searchString,@Min(0)@ApiParam(value = "number of records to skip for pagination") @Valid @RequestParam(value = "skip", required = false) Integer skip,@Min(0) @Max(50) @ApiParam(value = "maximum number of records to return") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("[ {  \"releaseDate\" : \"2016-08-29T09:12:33.001Z\",  \"name\" : \"Widget Adapter\",  \"id\" : \"d290f1ee-6c54-4b01-90e6-d701748f0851\",  \"manufacturer\" : {    \"phone\" : \"408-867-5309\",    \"name\" : \"ACME Corporation\",    \"homePage\" : \"https://www.acme-corp.com\"  }}, {  \"releaseDate\" : \"2016-08-29T09:12:33.001Z\",  \"name\" : \"Widget Adapter\",  \"id\" : \"d290f1ee-6c54-4b01-90e6-d701748f0851\",  \"manufacturer\" : {    \"phone\" : \"408-867-5309\",    \"name\" : \"ACME Corporation\",    \"homePage\" : \"https://www.acme-corp.com\"  }} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default InventoryApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}