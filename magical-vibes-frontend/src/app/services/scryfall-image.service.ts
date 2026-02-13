import { Injectable } from '@angular/core';

const DB_NAME = 'scryfall-cache';
const STORE_NAME = 'art-crops';
const DB_VERSION = 1;

interface QueueEntry {
  url: string;
  cacheKey: string;
  resolve: (objectUrl: string) => void;
  reject: (err: unknown) => void;
}

@Injectable({ providedIn: 'root' })
export class ScryfallImageService {

  private dbPromise: Promise<IDBDatabase>;
  private objectUrls = new Map<string, string>();
  private inFlight = new Map<string, Promise<string>>();
  private queue: QueueEntry[] = [];
  private draining = false;

  constructor() {
    this.dbPromise = this.openDb();
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
      return new Promise<string>((resolve, reject) => {
        this.queue.push({ url, cacheKey, resolve, reject });
        this.startDrain();
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

  private startDrain(): void {
    if (this.draining) return;
    this.draining = true;
    this.drainNext();
  }

  private drainNext(): void {
    const entry = this.queue.shift();
    if (!entry) {
      this.draining = false;
      return;
    }

    fetch(entry.url)
      .then(res => {
        if (!res.ok) throw new Error(`Scryfall returned ${res.status}`);
        return res.blob();
      })
      .then(blob => {
        this.putInDb(entry.cacheKey, blob).catch(() => { /* storage error */ });
        entry.resolve(this.toObjectUrl(entry.cacheKey, blob));
      })
      .catch(err => entry.reject(err))
      .finally(() => setTimeout(() => this.drainNext(), 100));
  }
}
