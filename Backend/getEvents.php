<?php
require 'engine/Library_7.0.php';

$data = new Query();
$result = $data->InvokeLoop('SELECT * FROM Events WHERE date_added <= NOW() && date_removed >= NOW()');

$output = array();

while($array = mysqli_fetch_array($result)) {
    $output []= array(
        "id" => (int)$array["id"],
        "name" => $array["name"],
        "type" => $array["type"],
        "latitude" => (float)$array["latitude"],
        "longitude"	 => (float)$array["longitude"]
    );
}

$data->Invoke('INSERT INTO Logs (timestamp) VALUES (NOW())');

header('Content-Type: application/json; charset=utf-8');
echo json_encode($output);
?>