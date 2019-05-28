package api;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.JsonMappingException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import exceptions.ApiException;
import http_model.StudentBase;
import model.Assignments;
import model.MyToken;
import model.Student;
import utils.Utils;



public class ApiService {
	
	private static final String URL_BASE = "http://notitas.herokuapp.com";
	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer ";
	private static ApiService instance;
	private static Client client;
	
	private static final String TAG = "ApiService";
	
	
	/**
	 * Crea una instancia de ApiService en caso de que no esté creada
	 * @param ninguno
	 * @return ninguno
	 */
	public static ApiService getInstance() { 
		if (instance == null)
			instance = new ApiService();
		return instance;
	}
	
	
	/**
	 * Genera el recursoWeb apuntando a URL_Base y al recurso que se pasa por parametro
	 *@param resource: recurso al cual se apunta
	 *@return WebResource: el webresource ya apuntando
	 */
	
	private WebResource generateWebReource(String resource) {
		if (client == null) { 
			ClientConfig cc = new DefaultClientConfig(); //Se instancia un ClientConfig por Default
			cc.getClasses().add(JacksonJsonProvider.class); //se utilizará Jackson para mapeo de objetos
			client = Client.create(cc); //Se crea el cliente configurado
		}
		return client.resource(URL_BASE).path(resource); //se apunta el cliente al resource y path
	}
	
	
	/**
	 * Realiza una peticion http Get al recurso correspondiente con el header indicado
	 *@param header: Tipo de header (Authorization, etc)
	 *@param resource: recurso al cual se quiere apuntar del endpoint
	 *@return response: la respuesta obtenida del endpoint
	 */
	private ClientResponse makeGetWithHeader (String header, String resource) throws ApiException {
		Utils.Log(TAG, "Resource: " + resource + "\nToken: " + MyToken.getInstance().getToken());
        ClientResponse response = generateWebReource(resource) 
                .header(header, "Bearer " + MyToken.getInstance().getToken())
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        
        checkResponseStatus(response.getClientResponseStatus());
        Utils.Log(TAG, "Status: " + response.getStatus());
        return response;
    }
	
	/**
	 * Realiza una peticion http get y retorna la respuesta "casteada" de Json a un objeto Assignments
	 *
	 */
	public Assignments getAssignments() throws ApiException {
		return makeGetWithHeader(AUTHORIZATION, Resources.assignments).getEntity(Assignments.class);
	}
	
	/**
	 * Realiza uan peticion http get y retorna la respuesta casteada de Json a un objeto StudentBase
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public StudentBase getStudent() throws ApiException {
		StudentBase stb = makeGetWithHeader(AUTHORIZATION, Resources.student).getEntity(StudentBase.class);
		Utils.Log(TAG, Utils.ObjectToJson(stb));
		return stb;
	}
	
	/**
	 * Realiza una peticion http put generando un builder apuntando al webResource indicado
	 * Con el builder se realiza la peticion put, mapeando el objeto student a formato Json para poder enviarlo al endpoint
	 */
	
	public void updateStudent (StudentBase student) throws ApiException {	
		Utils.Log(TAG, Utils.ObjectToJson(student));
		WebResource.Builder builder = generateWebReource(Resources.student)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.header(AUTHORIZATION, BEARER + MyToken.getInstance().getToken());
		
		ClientResponse response = builder.put(ClientResponse.class, Utils.ObjectToJson(student));
		checkResponseStatus(response.getClientResponseStatus());
	}
	
	/**
	 * Chequea que el status de la peticion realizada no sea ni 200 ni 201, caso contrario es Exception :)
	 */
	private void checkResponseStatus(Status status) throws ApiException {
		if (status.getStatusCode() != 200 && status.getStatusCode() != 201) throw new ApiException(status);
	}
}
