SET REFERENTIAL_INTEGRITY FALSE;

DELETE FROM app_info WHERE id IN (1,2,3);
DELETE FROM study_info WHERE id IN (1,2,3,4);
DELETE FROM locations WHERE id=2;
DELETE FROM sites WHERE id IN (1,2,3);
DELETE FROM user_details WHERE id IN (44, 45,46,47,48);
DELETE FROM participant_registry_site WHERE id IN (33,34,35);
DELETE FROM participant_study_info WHERE id IN (101, 102,103,104);
DELETE FROM auth_info WHERE id IN (222,223);
DELETE FROM user_app_details WHERE id=20;
DELETE FROM ur_admin_user WHERE id=1;
DELETE FROM app_permissions WHERE id=1;
DELETE FROM study_permissions WHERE id=1;
DELETE FROM sites_permissions WHERE id=1;


SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO `app_info`(`id`, `custom_app_id`, `created_time`, `app_name`, `created_by`, `updated_time`, `ios_authorization_token`, `ios_team_id`, `ios_bundle_id`, `android_server_key`, `ios_key_id`, `contact_us_to_email`, `feedback_to_email`, `from_email_id`, `app_status`, `android_latest_app_version`, `ios_latest_app_build_version`, `android_force_upgrade`, `ios_force_upgrade`, `app_support_email_address`) VALUES (1, 'GCPMS001', '2020-01-16 15:22:22', 'app-name-1', 0, '2020-03-12 15:17:56', 'LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJIa3dkd0lCQVFRZ2hVSlkwUnc1OWpZVDQzRVoKamUrN1BXb01iVlBHaWY1cThtTnI1R1pVNkxhZ0NnWUlLb1pJemowREFRZWhSQU5DQUFRbE9yZ2lIT0w0MzQ1cgpJNnBJNWh1YVdiTVU0dmM3WDRFYUVaYlZlS3hHbGFqNE1XUjl2ekZFVTMwNzlBYUdjdEp5RFZkRmcwb0hQOWhFCkN1STZyNjRnCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0=', '3485DDCN2M', 'com.btc.mystudiesgcp', 'AIzaSyDvFNoRTZJfrLnxU50wXbPDqjVBXcpQRa8', 'QU6XK35ALG', 'contactus_app_test@grr.la', 'feedback_app_test@grr.la', 'from_email_test@grr.la', 'Active', '1.0', '1.0', '0', '1', 'support_email@grr.la'), (2, 'GCPMS002', '2020-01-16 15:22:22', 'app-name-1', 0, '2020-03-12 15:17:56', 'LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJIa3dkd0lCQVFRZ2hVSlkwUnc1OWpZVDQzRVoKamUrN1BXb01iVlBHaWY1cThtTnI1R1pVNkxhZ0NnWUlLb1pJemowREFRZWhSQU5DQUFRbE9yZ2lIT0w0MzQ1cgpJNnBJNWh1YVdiTVU0dmM3WDRFYUVaYlZlS3hHbGFqNE1XUjl2ekZFVTMwNzlBYUdjdEp5RFZkRmcwb0hQOWhFCkN1STZyNjRnCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0=', '3485DDCN2M', 'com.btc.mystudiesgcp', 'AIzaSyDvFNoRTZJfrLnxU50wXbPDqjVBXcpQRa8', 'QU6XK35ALG', 'contactus_app_test@grr.la', 'feedback_app_test@grr.la', 'from_email_test@grr.la', 'Active', '1.0', '1.0', '0', '1', 'support_email@grr.la'),(3, 'GCPMS003', '2020-01-16 15:22:22', 'app-name-2', 0, '2020-03-12 15:17:56', 'LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUdTTTQ5QXdFSEJIa3dkd0lCQVFRZ2hVSlkwUnc1OWpZVDQzRVoKamUrN1BXb01iVlBHaWY1cThtTnI1R1pVNkxhZ0NnWUlLb1pJemowREFRZWhSQU5DQUFRbE9yZ2lIT0w0MzQ1cgpJNnBJNWh1YVdiTVU0dmM3WDRFYUVaYlZlS3hHbGFqNE1XUjl2ekZFVTMwNzlBYUdjdEp5RFZkRmcwb0hQOWhFCkN1STZyNjRnCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0=', '3485DDCN2M', 'com.btc.mystudiesgcp', 'AIzaSyDvFNoRTZJfrLnxU50wXbPDqjVBXcpQRa8', 'QU6XK35ALG', 'contactus_app_test@grr.la', 'feedback_app_test@grr.la', 'from_email_test@grr.la', 'Active', '1.0', '1.0', '0', '1', 'support_email@grr.la');

INSERT INTO `study_info` (`id`, `custom_id`, `app_info_id`, `name`, `description`, `type`,`created_by`, `created_time`, `updated_time`, `version`) VALUES (1, 'StudyID001', 1, 'name', 'description', 'OPEN', 0, '2020-03-12 15:23:41', '2020-03-12 15:24:42', '3.5'), (2, 'studyId1', 1, 'name', 'description', 'OPEN', 0, '2020-03-12 15:23:41', '2020-03-12 15:24:42', '3.6'), (3, 'custom-id-2', 1, 'name-2', 'description', 'CLOSED', 0, '2020-03-12 15:23:44', '2020-03-12 15:24:45', '3.7'), (4, '132', 1, 'name', 'description', 'OPEN', 0, '2020-03-12 15:23:41', '2020-03-12 15:24:42', '3.8');

INSERT INTO `locations` (`id`, `created_time`, `created_by`, `custom_id`, `description`, `is_default`, `name`, `status`) VALUES (2, '2020-03-17 18:59:15', 1, '-customId130.53', 'location-descp-updated', 'Y', 'name -1-updated000', '1');

INSERT INTO `sites` (`id`, `study_id`, `location_id`, `status`, `target_enrollment`, `name`, `created_time`, `created_by`) VALUES (1, 1, 2, 1, 10, 'test-site', '2020-03-17 20:19:42', 0), (2, 2, 2, 0, 15, 'test-site', '2020-03-12 15:19:38', 0), (3, 2, 2, 1, 45, 'test site', '2020-03-13 15:26:56', 0);

INSERT INTO `user_details` (`id`, `user_id`, `app_info_id`, `email`, `status`, `first_name`, `last_name`, `local_notification_flag`, `remote_notification_flag`,`touch_id`, `use_pass_code`, `verification_time`, `email_code`, `code_expire_time`) VALUES (44, 'kR2g5m2pJPP0P31-WNFYK8Al7jBP0mJ-cTSFJJHJ4DewuCg', 1, 'cdash93@gmail.com', 2, 'test', 'user', 0, 0, 0, 0, '2020-01-30 20:21:28', '1', '2040-07-22 13:02:10'), (45, 'kR2g5m2pJPP0P31-WNFYK8Al7jBP0mJ-cTSFJJHJ4DewuCj', 1, 'abc@xy', 2, 'test', 'user', 0, 0, 0, 0, '2020-01-30 20:21:28', '123code', '2020-07-22 13:02:10'), (46, 'kR2g5m2pJPP0P31-WNFYK8Al7jBP0mJ-cTSFJJHJ4DewuCh', 1, 'abc@gmail.com', 1, 'test', 'user', 0, 0, 0, 0, '2020-01-30 20:21:28', '123code', '2040-07-22 13:02:10'),(47, 'gnDoLZHKy0j27Eo-ap8cnZnvQvp7kBN-qylvroBJV7IssDk', 1, 'abc1234@gmail.com', 4, 'test', 'user', 0, 0, 0, 0, '2020-01-30 20:21:28', '1', '2030-07-22 13:02:10'),(48, 'gnDoLZHKy0j27Eo-ap8cnZnvQvp7kBN-qylvroBJV7D4FE', 3, 'abc1234@gmail.com', 4, 'test', 'user', 0, 0, 0, 0, '2020-01-30 20:21:28', '1', '2030-07-22 13:02:10');

INSERT INTO `participant_registry_site` (`id`, `site_id`, `study_info_id`, `email`, `invitation_time`, `onboarding_status`, `enrollment_token`, `enrollment_token_expiry`, `created_time`, `created_by`, `invitation_count`) VALUES (33, 1, 1, 'abc@gmail.com', '2020-02-07 20:37:25', 'I', 'dsgdsfgag', '2020-02-09 18:42:32', '2020-02-09 18:42:32', 2, 0), (34, 1, 1, 'xyz@gf.com', '2020-02-07 20:38:36', 'N', 'dfdsg', '2020-02-09 18:42:34', '2020-02-09 18:42:32', 3, 0), (35, 1, 1, 'pqr@gf.com', '2020-02-07 20:38:36', 'D', 'dfdsgr', '2020-02-09 18:42:34', '2020-02-09 18:42:32', 3, 0);

INSERT INTO `participant_study_info` (`id`, `participant_id`, `study_info_id`, `participant_registry_site_id`, `site_id`, `user_details_id`, `status`, `bookmark`, `enrolled_timestamp`, `completion`, `adherence`, `withdrawal_timestamp`) VALUES (101, '1', 2, 33, 1, 44, 'yetToJoin', 1, '2020-02-06 14:07:29', 45, 20, '2020-02-10 14:03:14'), (102, '2', 1, 34, 1, 44, 'Enrolled', 0, '2020-02-06 14:07:31', 50, 36, '2020-02-06 14:07:31'), (103, '3', 4, 33, 1, 44, 'withdrawn', 1, '2020-02-06 14:07:29', 45, 20, '2020-02-10 14:03:14'), (104, '4', 2, 33, 1, 47, 'yetToJoin', 1, '2020-02-06 14:07:29', 45, 20, '2020-02-10 14:03:14');

INSERT INTO `auth_info` (`id`, `app_info_id`, `created_time`, `device_token`, `device_type`, `ios_app_version`, `updated_time`, `remote_notification_flag`, `user_details_id`) VALUES (222, 1, '2020-03-22 17:12:23', '7B3F1433E1157D370EE8A6BA2E612B27EC2C0081771FC8BA1ECFCBB07BBA5A17', 'ios', '1.0.1', '2020-03-27 11:28:58', 1, 44), (223, 1, '2020-03-22 17:12:23', 'd3FncrcvPbw:APA91bHYlhS69KhhxIx8uoD152GT6JdfzF-bpmJiS04E9wVJapS5ELJwhwthRlDKPHR3OrAEap13-TxLc5EIc4OGME1iDRi04W3LYRtYkCdPQ76h5rTj1RTVT7q4SMASMVozESR2-gZl', 'android', '', '2020-03-27 11:28:58', 1, 44);

INSERT INTO `user_app_details` (`id`, `app_info_id`, `created_time`, `user_details_id`) VALUES (20, 1, '2020-03-21 08:49:38', 44);

INSERT INTO `ur_admin_user` (`id`, `created_time`, `email`, `first_name`, `last_name`, `super_admin`) VALUES ('1', '2020-08-05 18:51:37', 'mock@gmail.com', 'test', 'user', '0');

INSERT INTO `app_permissions` (`id`, `created_time`, `created_by`, `edit`, `app_info_id`, `ur_admin_user_id`) VALUES ('1', '2020-08-05 18:42:42', '0','1', '1', '1');

INSERT INTO `study_permissions` (`id`, `created_time`, `created_by`, `edit`, `app_info_id`, `study_id`, `ur_admin_user_id`) VALUES ('1', '2020-08-05 18:43:33', '0', '1', '1', '1', '1');

INSERT INTO `sites_permissions` (`id`, `created_time`, `created_by`, `edit`, `app_info_id`, `study_id`, `site_id`, `ur_admin_user_id`) VALUES ('1', '2020-08-05 18:43:33', '1', '0', '1', '1', '1', '1');