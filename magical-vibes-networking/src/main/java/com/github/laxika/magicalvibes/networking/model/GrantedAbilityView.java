package com.github.laxika.magicalvibes.networking.model;

/**
 * One effective ability added to a permanent beyond its printed rules text.
 *
 * @param text       display-ready rules text for the granted ability
 * @param sourceName display name of the source that granted it, or {@code null} when the
 *                   legacy runtime state does not retain source attribution
 */
public record GrantedAbilityView(String text, String sourceName) {
}
