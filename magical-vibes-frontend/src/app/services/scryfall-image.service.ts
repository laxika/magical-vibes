import { Injectable, inject } from '@angular/core';
import { ScryfallFetchQueue } from './scryfall-fetch-queue.service';

const DB_NAME = 'scryfall-cache';
const STORE_NAME = 'art-crops';
const DB_VERSION = 1;

@Injectable({ providedIn: 'root' })
export class ScryfallImageService {

  private dbPromise: Promise<IDBDatabase>;
  private objectUrls = new Map<string, string>();
  private inFlight = new Map<string, Promise<string>>();
  private fetchQueue = inject(ScryfallFetchQueue);

  constructor() {
    this.dbPromise = this.openDb();
  }

  getCachedArtCropUrl(setCode: string, collectorNumber: string): string | null {
    return this.objectUrls.get(`${setCode}:${collectorNumber}`) ?? null;
  }

  getArtCropUrl(setCode: string, collectorNumber: string): Promise<string> {
    const cacheKey = `${setCode}:${collectorNumber}`;

    const memCached = this.objectUrls.get(cacheKey);
    if (memCached) {
      return Promise.resolve(memCached);
    }

    const existing = this.inFlight.get(cacheKey);
    if (existing) {
      return existing;
    }

    const url = `https://api.scryfall.com/cards/${encodeURIComponent(setCode)}/${encodeURIComponent(collectorNumber)}?format=image&version=art_crop`;

    const promise = this.getFromDb(cacheKey).then(blob => {
      if (blob) {
        return this.toObjectUrl(cacheKey, blob);
      }
      return this.fetchQueue.enqueue(url).then(fetchedBlob => {
        this.putInDb(cacheKey, fetchedBlob).catch(() => {});
        return this.toObjectUrl(cacheKey, fetchedBlob);
      });
    });

    this.inFlight.set(cacheKey, promise);
    promise.finally(() => this.inFlight.delete(cacheKey));

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

  private toObjectUrl(cacheKey: string, blob: Blob): string {
    const objectUrl = URL.createObjectURL(blob);
    this.objectUrls.set(cacheKey, objectUrl);
    return objectUrl;
  }
}
