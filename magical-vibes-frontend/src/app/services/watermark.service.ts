import { Injectable } from '@angular/core';

const DB_NAME = 'watermark-cache';
const STORE_NAME = 'watermarks';
const DB_VERSION = 1;
const BASE_URL = 'https://raw.githubusercontent.com/Investigamer/mtg-vectors/main/svg/optimized/watermark';

@Injectable({ providedIn: 'root' })
export class WatermarkService {

  private dbPromise: Promise<IDBDatabase>;
  private objectUrls = new Map<string, string>();
  private inFlight = new Map<string, Promise<string>>();

  constructor() {
    this.dbPromise = this.openDb();
  }

  getCachedWatermarkUrl(watermark: string): string | null {
    return this.objectUrls.get(watermark) ?? null;
  }

  getWatermarkUrl(watermark: string): Promise<string> {
    const cached = this.objectUrls.get(watermark);
    if (cached) {
      return Promise.resolve(cached);
    }

    const existing = this.inFlight.get(watermark);
    if (existing) {
      return existing;
    }

    const url = `${BASE_URL}/${encodeURIComponent(watermark)}.svg`;

    const promise = this.getFromDb(watermark).then(blob => {
      if (blob) {
        return this.toObjectUrl(watermark, blob);
      }
      return fetch(url).then(res => {
        if (!res.ok) throw new Error(`Watermark fetch returned ${res.status}`);
        return res.blob();
      }).then(fetchedBlob => {
        this.putInDb(watermark, fetchedBlob).catch(() => {});
        return this.toObjectUrl(watermark, fetchedBlob);
      });
    });

    this.inFlight.set(watermark, promise);
    promise.finally(() => this.inFlight.delete(watermark));

    return promise;
  }

  private openDb(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);
      request.onupgradeneeded = () => {
        request.result.createObjectStore(STORE_NAME);
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  private async getFromDb(key: string): Promise<Blob | undefined> {
    const db = await this.dbPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const req = tx.objectStore(STORE_NAME).get(key);
      req.onsuccess = () => resolve(req.result ?? undefined);
      req.onerror = () => reject(req.error);
    });
  }

  private async putInDb(key: string, blob: Blob): Promise<void> {
    const db = await this.dbPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      const req = tx.objectStore(STORE_NAME).put(blob, key);
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }

  private toObjectUrl(key: string, blob: Blob): string {
    const objectUrl = URL.createObjectURL(blob);
    this.objectUrls.set(key, objectUrl);
    return objectUrl;
  }
}
