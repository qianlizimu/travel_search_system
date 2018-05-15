'use strict';

var express=require("express");
var http=require('http');
var https = require("https");
var app=express();
const yelp = require('yelp-fusion');
const apiKey = 'i_uAXf4dVmFGfV_-WXLVmDqt1VkRdl1D6-who1gxUvbcwo70xoVXU-AfkJ7NOkKH78nNPGGsN7zutwJAc42nYlpZ0QidvzP8ZoydYeXispirLy5S6n6DG53NsRi5WnYx';
const client = yelp.client(apiKey);

app.listen(8081);


app.get('/next_page', function(req, res){

    function next_token_search( tokens ){
        https.get('https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken='+
            tokens+"&key=AIzaSyBaZkCvf4pZ-8Y155R-4cveEo9jeObf-Jc",
            function( res2){
                var html='';
                res2.on('data',function(data){
                    html+=data;
                });
                res2.on('end',function(){
                    var    ressult_2=JSON.parse(html);
                        if (ressult_2.results.length==0){
                            setTimeout( function(){next_token_search( tokens ); }, 1000);
                        } else {
                            res.send(html);
                        }
                });
            });
    }

    next_token_search(req.query.token );

});

app.get('/submit_form', function(req, res) {

    var from_lat, from_lon;

    function nearby_search(lat, lon){
        https.get('https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='+lat+","+lon+"&radius="
           +req.query.distance+ "&type="+req.query.categorys+"&keyword="+encodeURI(req.query.keyword)+"&key=AIzaSyBaZkCvf4pZ-8Y155R-4cveEo9jeObf-Jc",
            function( res2){
                var html='';
                res2.on('data',function(data){
                    html+=data;
                });
                res2.on('end',function(){
                  res.send(html);
                });
            });
    }

    if (req.query.fromplace=="other"){

        https.get('https://maps.googleapis.com/maps/api/geocode/json?address='+encodeURI(req.query.input_location)+
            "&key=AIzaSyBaZkCvf4pZ-8Y155R-4cveEo9jeObf-Jc",
            function( res2){
            var html='';
            res2.on('data',function(data){
                html+=data;
            });
            res2.on('end',function(){
                var geo_res=JSON.parse(html);
                if (geo_res.hasOwnProperty("results")==false ||
                    (geo_res.hasOwnProperty("results") && geo_res.results.length==0) ){
                    res.send(JSON.stringify({results:[]}));
                    return;
                }
                 from_lat=geo_res.results[0].geometry.location.lat;
                 from_lon=geo_res.results[0].geometry.location.lng;
                 nearby_search(from_lat, from_lon);
            });
        });
    } else {
        from_lat=req.query.here_lat;
        from_lon=req.query.here_lon;
        nearby_search(from_lat, from_lon);
    }

});

app.get('/geocode_place', function(req, res){
    https.get('https://maps.googleapis.com/maps/api/geocode/json?address='+encodeURI(req.query.name)+
        "&key=AIzaSyBaZkCvf4pZ-8Y155R-4cveEo9jeObf-Jc",
        function( res2){
            var html='';
            res2.on('data',function(data){
                html+=data;
            });
            res2.on('end',function(){
                var geo_res=JSON.parse(html);
                if (geo_res.hasOwnProperty("results")==false ||
                    (geo_res.hasOwnProperty("results") && geo_res.results.length==0) ){
                    res.send("none");
                    return;
                }
               var from_lat=geo_res.results[0].geometry.location.lat;
              var  from_lon=geo_res.results[0].geometry.location.lng;
              var placeid=geo_res.results[0].place_id;
              var res_txt=JSON.stringify({lat:from_lat, lng:from_lon, place_id:placeid});
                res.send(res_txt);
            });
        });
});

app.get('/get_yelp', function(req, res) {

   function get_review(id){
       client.reviews(id).then(response => {
        //   console.log(response.jsonBody.reviews[0].text);
           res.send(JSON.stringify( response.jsonBody.reviews) );
   }).catch(e => {
           console.log(e);
   });
    }

    client.businessMatch('best', {
        name: req.query.name,
        address1: req.query.address1,
        city: req.query.city,
        state: req.query.state,
        country: req.query.country
      }).then(response => {
        if (response.jsonBody.businesses.length==0){
           res.send("none");
           return;
      } else
    {
       get_review(response.jsonBody.businesses[0].id);
    }   }).catch(e => {
        console.log(e);
      });

});

app.get("/get_routes", function(req, res){
    https.get('https://maps.googleapis.com/maps/api/directions/json?origin=place_id:'+req.query.st_place_id+
        "&destination="+req.query.des_lat+","+req.query.des_lng+"&mode="+req.query.mode+"&key=AIzaSyBaZkCvf4pZ-8Y155R-4cveEo9jeObf-Jc",
        function( res2){
            var html='';
            res2.on('data',function(data){
                html+=data;
            });
            res2.on('end',function(){
                var geo_res=JSON.parse(html);
                if (geo_res.hasOwnProperty("status") && geo_res.status=="OK"){
                    res.send(html);
                    return;
                } else {
                    res.send("none");
                    return;
                }
            });
        });
});

app.get("/place_detail", function(req, res){
    https.get("https://maps.googleapis.com/maps/api/place/details/json?placeid="+req.query.placeid+
       "&key=AIzaSyBaZkCvf4pZ-8Y155R-4cveEo9jeObf-Jc",
        function( res2){
            var html='';
            res2.on('data',function(data){
                html+=data;
            });
            res2.on('end',function(){
                res.send(html);
            });
        });
});
