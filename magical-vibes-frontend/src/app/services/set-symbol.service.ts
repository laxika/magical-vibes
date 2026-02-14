import { Injectable, inject, signal } from '@angular/core';
import { ScryfallFetchQueue } from './scryfall-fetch-queue.service';

const DB_NAME = 'set-symbols-cache';
const STORE_NAME = 'symbols';
const DB_VERSION = 1;

@Injectable({ providedIn: 'root' })
export class SetSymbolService {

  private symbolUrls = new Map<string, string>();
  private dbPromise: Promise<IDBDatabase>;
  private inFlight = new Map<string, Promise<string>>();
  private fetchQueue = inject(ScryfallFetchQueue);

  /** Increments as symbols load — read in templates/computed to trigger re-renders */
  symbolsVersion = signal(0);

  constructor() {
    this.dbPromise = this.openDb();
  }

  getSymbolUrl(setCode: string): string | null {
    this.symbolsVersion();
    const key = setCode.toLowerCase();
    const url = this.symbolUrls.get(key);
    if (url) return url;
    this.ensureLoaded(key);
    return null;
  }

  private ensureLoaded(key: string): void {
    if (this.symbolUrls.has(key) || this.inFlight.has(key)) return;
    this.loadSymbol(key);
  }

  private loadSymbol(key: string): Promise<string> {
    const existing = this.inFlight.get(key);
    if (existing) return existing;

    const cached = this.symbolUrls.get(key);
    if (cached) return Promise.resolve(cached);

    const url = `https://svgs.scryfall.io/sets/${encodeURIComponent(key)}.svg`;

    const promise = this.getFromDb(key).then(blob => {
      if (blob) {
        return this.toObjectUrl(key, blob);
      }
      return this.fetchQueue.enqueue(url).then(fetchedBlob => {
        this.putInDb(key, fetchedBlob).catch(() => {});
        return this.toObjectUrl(key, fetchedBlob);
      });
    });

    this.inFlight.set(key, promise);
    promise
      .then(() => this.symbolsVersion.update(v => v + 1))
      .catch(() => {})
      .finally(() => this.inFlight.delete(key));

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
    this.symbolUrls.set(key, objectUrl);
    return objectUrl;
  }
}
