import { Injectable } from '@angular/core';

interface QueueEntry {
  url: string;
  responseType: 'blob' | 'json';
  resolve: (value: any) => void;
  reject: (err: unknown) => void;
}

/**
 * Shared rate-limited fetch queue for all Scryfall requests.
 * Enforces 100ms minimum delay between requests across all consumers.
 */
@Injectable({ providedIn: 'root' })
export class ScryfallFetchQueue {

  private queue: QueueEntry[] = [];
  private draining = false;

  /**
   * Enqueue a fetch to Scryfall. Returns a promise that resolves with the response Blob.
   */
  enqueue(url: string): Promise<Blob> {
    return new Promise<Blob>((resolve, reject) => {
      this.queue.push({ url, responseType: 'blob', resolve, reject });
      this.startDrain();
    });
  }

  /**
   * Enqueue a fetch to Scryfall. Returns a promise that resolves with parsed JSON.
   */
  enqueueJson<T>(url: string): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      this.queue.push({ url, responseType: 'json', resolve, reject });
      this.startDrain();
    });
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
        return entry.responseType === 'json' ? res.json() : res.blob();
      })
      .then(value => entry.resolve(value))
      .catch(err => entry.reject(err))
      .finally(() => setTimeout(() => this.drainNext(), 100));
  }
}
