'use strict';

// Users service used for communicating with the users REST endpoint
angular.module('users').factory('Users', ['$resource',
                                      	function($resource) {
	return $resource('rest/users/:userId', { userId: '@id'
	}, {
		update: {
			method: 'PUT'
		}, 
		getProfile: {
			url: 'rest/users/profile/:userId'
		},
		updateProfile:{
			method: 'PUT',
			url: 'rest/users/profile'
		}
	})}
]);