/**
 * Formats a SCREAMING_SNAKE_CASE enum value to Title Case.
 * e.g. "LEGENDARY_CREATURE" → "Legendary Creature"
 */
export function formatEnumName(s: string): string {
  return s.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ');
}

/**
 * Formats a list of keyword enum values to a comma-separated display string.
 * e.g. ["FIRST_STRIKE", "FLYING"] → "First strike, Flying"
 */
export function formatKeywords(keywords: string[]): string {
  return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase().replace('_', ' ')).join(', ');
}
