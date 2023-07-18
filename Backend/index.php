<?php

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$datasets = array(
        "Lifts and Escalators" => "https://opendata.emel.pt/pedestrian/liftsandescalators",
        "Offstret parking areas" => "https://opendata.emel.pt/offstreetparking/lots/list",
        "Metro Stations" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POITransportes/FeatureServer/1/query?outFields=*&where=1%3D1&f=geojson",
        "Traffic Closures" => "https://opendata.emel.pt/traffic/closures/list",
        "Tunnels" => "https://opendata.emel.pt/traffic/tunnels",
        "Sports Centers" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/Desporto_CentrosDesportivos/FeatureServer/0/query?outFields=*&where=1%3D1&f=geojson",
        "Religious Buildings" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/Patrimonio/FeatureServer/6/query?where=1%3D1&outFields=*&f=pgeojson",
        "Railway Stations" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POITransportes/FeatureServer/1/query?where=1%3D1&outFields=*&f=pgeojson",
        "Local Tax Offices" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/Administracao_Publica/FeatureServer/0/query?outFields=*&where=1%3D1&f=geojson",
        "Public Hospitals" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/ArcGIS/rest/services/POISaude/FeatureServer/4/query?where=1%3D1&outFields=*&f=pgeojson",
        "Museums" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/EconomiaCriativa/FeatureServer/8/query?where=1%3D1&outFields=*&f=pgeojson",
        "Theatres" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POICultura/FeatureServer/4/query?where=1%3D1&outFields=*&f=pgeojson",
        "Dog Parks" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/ParquesCaninos/FeatureServer/0/query?where=1%3D1&outFields=*&f=pgeojson",
        "Cultural Centers" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POICultura/FeatureServer/7/query?where=1%3D1&outFields=*&f=pgeojson",
        "Private School 2nd cycle" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/7/query?where=1%3D1&outFields=*&f=pgeojson",
        "Private Schools 1st cycle" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/6/query?where=1%3D1&outFields=*&f=pgeojson",
        "Private Pre-School" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/5/query?where=1%3D1&outFields=*&f=pgeojson",
        "Private Secondary school" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/8/query?where=1%3D1&outFields=*&f=pgeojson",
        "Public School 1st cycle" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/1/query?where=1%3D1&outFields=*&f=pgeojson",
        "Health Centers" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/ArcGIS/rest/services/POISaude/FeatureServer/0/query?where=1%3D1&outFields=*&f=pgeojson",
        "Public School 2nd cycle" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/2/query?where=1%3D1&outFields=*&f=pgeojson", 
        "Public School 3rd cycle" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/3/query?where=1%3D1&outFields=*&f=pgeojson",
        "Public Secondary school" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIEducacao/FeatureServer/4/query?where=1%3D1&outFields=*&f=pgeojson",
        "Private Hospitals" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POISaude/FeatureServer/3/query?where=1%3D1&outFields=*&f=pgeojson",
        "Parks" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIArLivre/FeatureServer/0/query?where=1%3D1&outFields=*&f=pgeojson",
        "Markets" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIComprar/FeatureServer/1/query?where=1%3D1&outFields=*&f=pgeojson",
        "Cementeries" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POIServicos/FeatureServer/0/query?where=1%3D1&outFields=*&f=pgeojson",
        "Firefighters" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POISocorro/FeatureServer/0/query?where=1%3D1&outFields=*&f=pgeojson",
        "Auditorium" => "https://services.arcgis.com/1dSrzEWVQn5kHHyK/arcgis/rest/services/POICultura/FeatureServer/1/query?where=1%3D1&outFields=*&f=pgeojson"
);

$responses = [];

foreach($datasets as $name => $url) {
    $curl = curl_init();
    
    curl_setopt_array($curl, array(
        CURLOPT_URL => $url,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_ENCODING => "",
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
        CURLOPT_CUSTOMREQUEST => "GET",
        CURLOPT_HTTPHEADER => array("cache-control: no-cache"),
    ));
    
    $response = curl_exec($curl);
    $error = curl_error($curl);
    
    curl_close($curl);
    
    if(!$error) {
        $data = json_decode($response);

        switch($name) {
            case "Lifts and Escalators":
                for($int = 0; $int < count($data); $int++) {
                    $output = array(
                        "Type" =>  "Elevador ou escada rolante",
                        "Category" => 1,
                        "Name" => $data[$int]->localizacao,
                        "Latitude" => (float) $data[$int]->coordenadas[1],
                        "Longitude" => (float) $data[$int]->coordenadas[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Offstret parking areas":
                for($int = 0; $int < count($data); $int++) {
                    $output = array(
                        "Type" =>  "Área de estacionamento",
                        "Category" => 2,
                        "Name" => $data[$int]->parque,
                        "Latitude" => (float) $data[$int]->localizacao->latitude,
                        "Longitude" => (float) $data[$int]->localizacao->longitude
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Metro Stations":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Estação de metrô",
                        "Category" => 3,
                        "Name" => $data->features[$int]->properties->NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Traffic Closures":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Fechamento do trânsito",
                        "Category" => 4,
                        "Name" => $data->features[$int]->properties->restricao_circulacao." pela razão de ".strtolower($data->features[$int]->properties->motivo),
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[0][0][1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0][0][0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Tunnels":
                for($int = 0; $int < count($data); $int++) {
                    $output = array(
                        "Type" =>  "Túnel",
                        "Category" => 5,
                        "Name" => $data[$int]->localizacao,
                        "Latitude" => (float) $data[$int]->latitude,
                        "Longitude" => (float) $data[$int]->longitude
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Sports Centers":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Centro esportivo",
                        "Category" => 6,
                        "Name" => $data->features[$int]->properties->DESIGNACAO,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[0][0][1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0][0][0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Religious Buildings":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Centro religioso",
                        "Category" => 7,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Railway Stations":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Estação ferroviária",
                        "Category" => 8,
                        "Name" => $data->features[$int]->properties->NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Local Tax Offices":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Autoridade tributária local",
                        "Category" => 9,
                        "Name" => $data->features[$int]->properties->NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Public Hospitals":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Hospital público",
                        "Category" => 10,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Museums":
                for($int = 0; $int < count($data->features); $int++) {
                    if($data->features[$int]->geometry <> NULL) {
                        $output = array(
                            "Type" =>  "Museu",
                            "Category" => 11,
                            "Name" => $data->features[$int]->properties->NOME,
                            "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                            "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                        );
                        
                        $responses[] = $output;
                    }   
                }
            break;
            case "Theatres":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Teatro",
                        "Category" => 12,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Dog Parks":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Parque canino",
                        "Category" => 13,
                        "Name" => $data->features[$int]->properties->DESIGNACAO,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[0][0][1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0][0][0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Cultural Centers":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Centro cultural",
                        "Category" => 14,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Private School 2nd cycle":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Escola privada de segundo ciclo",
                        "Category" => 15,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Private Schools 1st cycle":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Escola privada de primeiro ciclo",
                        "Category" => 16,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Private Pre-School":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Pré-escola privada",
                        "Category" => 17,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Private Secondary school":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Escola secundária privada",
                        "Category" => 18,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Public School 1st cycle":
                for($int = 0; $int < count($data->features); $int++) {
                    if($data->features[$int]->geometry <> NULL) {
                        $output = array(
                            "Type" =>  "Escola pública de primeiro ciclo",
                            "Category" => 19,
                            "Name" => $data->features[$int]->properties->NOME_ESCOLA,
                            "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                            "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                        );
                        
                        $responses[] = $output;
                    }
                }
            break;
            case "Health Centers":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Centro de Saúde",
                        "Category" => 20,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Public School 2nd cycle":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Escola pública de segundo ciclo",
                        "Category" => 21,
                        "Name" => $data->features[$int]->properties->NOME_ESCOLA,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Public School 3rd cycle":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Escola pública de terceiro ciclo",
                        "Category" => 22,
                        "Name" => $data->features[$int]->properties->NOME_ESCOLA,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Public Secondary school":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Escola secundária pública",
                        "Category" => 23,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Private Hospitals":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Hospital privado",
                        "Category" => 24,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Parks":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Parque",
                        "Category" => 25,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Markets":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Feira",
                        "Category" => 26,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Cementeries":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Cemitério",
                        "Category" => 27,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Firefighters":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Bombeiros",
                        "Category" => 28,
                        "Name" => $data->features[$int]->properties->NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
            case "Auditorium":
                for($int = 0; $int < count($data->features); $int++) {
                    $output = array(
                        "Type" =>  "Auditório",
                        "Category" => 29,
                        "Name" => $data->features[$int]->properties->INF_NOME,
                        "Latitude" => (float) $data->features[$int]->geometry->coordinates[1],
                        "Longitude" => (float) $data->features[$int]->geometry->coordinates[0]
                    );
                    
                    $responses[] = $output;
                }
            break;
        }
        
        
    }
}

header('Content-Type: application/json; charset=utf-8');
print(json_encode($responses));
?>