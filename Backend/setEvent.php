<?php
require 'engine/Library_7.0.php';

$data = new Query();
$success = false;

if($_GET["key"] == "0CteP3mPexoVQDM6CCzf") {
   $result = $data->Invoke('INSERT INTO Events (name, type, latitude, longitude, date_added, date_removed) VALUES ("'.$_GET["name"].'", "'.$_GET["type"].'", '.$_GET["latitude"].', '.$_GET["longitude"].', NOW(), NOW() + INTERVAL '.$_GET["hours"].' HOUR)');
   
   if($result) {
       $success = true;
   } 
}

$output = array("success" => $success);

header('Content-Type: application/json; charset=utf-8');
echo json_encode($output);
?>