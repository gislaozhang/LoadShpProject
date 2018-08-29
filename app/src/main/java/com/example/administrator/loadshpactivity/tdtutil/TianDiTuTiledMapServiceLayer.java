package com.example.administrator.loadshpactivity.tdtutil;

import android.os.Environment;
import android.util.Log;

import com.esri.android.map.TiledServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.UserCredentials;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

public class TianDiTuTiledMapServiceLayer extends TiledServiceLayer {
	private TianDiTuTiledMapServiceType _mapType;
	private TileInfo tiandituTileInfo;
	private static String cachepath = Environment.getExternalStorageDirectory()
			.toString() + "/zjgis/cache";

	public static String getCachepath() {
		return cachepath;
	}

	public TianDiTuTiledMapServiceLayer() {
		this(null, null, true);
	}

	public TianDiTuTiledMapServiceLayer(TianDiTuTiledMapServiceType mapType) {
		this(mapType, null, true);
	}

	public TianDiTuTiledMapServiceLayer(TianDiTuTiledMapServiceType mapType,
			UserCredentials usercredentials) {
		this(mapType, usercredentials, true);
	}

	public TianDiTuTiledMapServiceLayer(TianDiTuTiledMapServiceType mapType,
			UserCredentials usercredentials, boolean flag) {
		super("");
		this._mapType = mapType;
		setCredentials(usercredentials);

		if (flag)
			try {
				getServiceExecutor().submit(new Runnable() {

					public final void run() {
						a.initLayer();
					}

					final TianDiTuTiledMapServiceLayer a;

					{
						a = TianDiTuTiledMapServiceLayer.this;
						// super();
					}
				});
				return;
			} catch (RejectedExecutionException _ex) {
			}
	}

	public TianDiTuTiledMapServiceType getMapType() {
		return this._mapType;
	}

	protected void initLayer() {
		this.buildTileInfo();
		this.setFullExtent(new Envelope(-180, -90, 180, 90));
		this.setDefaultSpatialReference(SpatialReference.create(4490)); // CGCS2000
		// this.setDefaultSpatialReference(SpatialReference.create(4326));
		this.setInitialExtent(new Envelope(90.52, 33.76, 113.59, 42.88));
		super.initLayer();
	}

	public void refresh() {
		try {
			getServiceExecutor().submit(new Runnable() {

				public final void run() {
					if (a.isInitialized())
						try {
							a.b();
							a.clearTiles();
							return;
						} catch (Exception exception) {
							Log.e("ArcGIS",
									"Re-initialization of the layer failed.",
									exception);
						}
				}

				final TianDiTuTiledMapServiceLayer a;

				{
					a = TianDiTuTiledMapServiceLayer.this;
					// super();
				}
			});
			return;
		} catch (RejectedExecutionException _ex) {
			return;
		}
	}

	final void b() throws Exception {

	}

	@Override
	protected byte[] getTile(int level, int col, int row) throws Exception {
		// 看本地是否有
		byte[] result = getOfflineCacheFile(level, col, row, _mapType);

		// 从网络获取
		if (result == null) {

			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				URL sjwurl = new URL(this.getTianDiMapUrl(level, col, row));
				HttpURLConnection httpUrl = null;
				BufferedInputStream bis = null;
				byte[] buf = new byte[1024];

				httpUrl = (HttpURLConnection) sjwurl.openConnection();
				httpUrl.connect();
				bis = new BufferedInputStream(httpUrl.getInputStream());

				while (true) {
					int bytes_read = bis.read(buf);
					if (bytes_read > 0) {
						bos.write(buf, 0, bytes_read);
					} else {
						break;
					}
				}
				;
				bis.close();
				httpUrl.disconnect();

				result = bos.toByteArray();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			AddOfflineCacheFile(level, col, row, _mapType, result);

		}

		return result;
	}

	@Override
	public TileInfo getTileInfo() {
		return this.tiandituTileInfo;
	}

	/**
     * 
     * */
	private String getTianDiMapUrl(int level, int col, int row) {

		String url = new TDTUrl(level, col, row, this._mapType).generatUrl();
		return url;
	}

	private void buildTileInfo() {
		Point originalPoint = new Point(-180, 90);

		double[] res = { 1.40625, 0.703125, 0.3515625, 0.17578125, 0.087890625,
				0.0439453125, 0.02197265625, 0.010986328125, 0.0054931640625,
				0.00274658203125, 0.001373291015625, 0.0006866455078125,
				0.00034332275390625, 0.000171661376953125,
				8.58306884765629E-05, 4.29153442382814E-05,
				2.14576721191407E-05, 1.07288360595703E-05,
				5.36441802978515E-06, 2.68220901489258E-06,
				1.34110450744629E-06 };
		double[] scale = { 400000000, 295497598.5708346, 147748799.285417,
				73874399.6427087, 36937199.8213544, 18468599.9106772,
				9234299.95533859, 4617149.97766929, 2308574.98883465,
				1154287.49441732, 577143.747208662, 288571.873604331,
				144285.936802165, 72142.9684010827, 36071.4842005414,
				18035.7421002707, 9017.87105013534, 4508.93552506767,
				2254.467762533835, 1127.2338812669175, 563.616940 };

		int levels = 21;
		int dpi = 96;
		int tileWidth = 256;
		int tileHeight = 256;

		this.tiandituTileInfo = new TileInfo(
				originalPoint, scale, res, levels, dpi, tileWidth, tileHeight);
		this.setTileInfo(this.tiandituTileInfo);
	}

	// 将图片保存到本地 目录结构可以随便定义 只要你找得到对应的图片
	private byte[] AddOfflineCacheFile(int level, int col, int row,
			TianDiTuTiledMapServiceType mapType, byte[] bytes) {
		File file = new File(cachepath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File levelfile = new File(cachepath + File.separator + level);
		if (!levelfile.exists()) {
			levelfile.mkdirs();
		}
		File colfile = new File(cachepath + File.separator + level
				+ File.separator + col);
		if (!colfile.exists()) {
			colfile.mkdirs();
		}
		File rowfile = new File(cachepath + File.separator + level
				+ File.separator + col + File.separator + row);
		if (!rowfile.exists()) {
			colfile.mkdirs();
		}

		File rowfile2 = new File(cachepath + File.separator + level
				+ File.separator + col + File.separator + row + mapType
				+ ".dat");
		if (!rowfile2.exists()) {
			try {
				FileOutputStream out = new FileOutputStream(rowfile2);
				out.write(bytes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return bytes;

	}

	// 从本地获取图片
	private byte[] getOfflineCacheFile(int level, int col, int row,
			TianDiTuTiledMapServiceType maptype) {
		byte[] bytes = null;
		File rowfile = new File(cachepath + "/" + level + "/" + col + "/" + row
				+ maptype + ".dat");
		if (rowfile.exists()) {
			try {
				bytes = CopySdcardbytes(rowfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			bytes = null;
		}
		return bytes;
	}

	// 读取本地图片流
	public byte[] CopySdcardbytes(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);

		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

		byte[] temp = new byte[1024];

		int size = 0;

		while ((size = in.read(temp)) != -1) {
			out.write(temp, 0, size);
		}
		in.close();
		byte[] bytes = out.toByteArray();
		return bytes;
	}

}
