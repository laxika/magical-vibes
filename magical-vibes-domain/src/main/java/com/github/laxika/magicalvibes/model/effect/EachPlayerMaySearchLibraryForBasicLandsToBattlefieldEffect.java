package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may search their library for up to {@code count} basic land cards, put them onto the
 * battlefield untapped, then shuffle. Players search in APNAP order (CR 101.4) and each search is
 * optional ("may"), so a player may take fewer than {@code count} cards, or none.
 *
 * <p>Used by Veteran Explorer.
 */
public record EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(int count) implements CardEffect {
}
