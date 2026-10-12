package com.github.laxika.magicalvibes.model;

/**
 * One "deals that much damage plus N" effect created for a turn or perpetually (The Flame of Keld,
 * Taii Wakeen): {@code sourceName} names the card that created it, {@code color} limits it to
 * sources of that color ({@code null} for any source) and {@code amount} is N. Each grant is a
 * separate replacement effect, so several are applied one at a time.
 */
public record DamageBonusGrant(String sourceName, CardColor color, int amount) {}
