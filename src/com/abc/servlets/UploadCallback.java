package com.abc.servlets;

public interface UploadCallback<T> {
	public void onSuccess(T t);
}
