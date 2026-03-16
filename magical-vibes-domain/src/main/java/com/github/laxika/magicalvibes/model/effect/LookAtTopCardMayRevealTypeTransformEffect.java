package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Looks at the top card of the controller's library. The controller may reveal that card
 * (regardless of type). If the revealed card matches one of the specified card types,
 * transforms the source permanent. The card stays on top of the library.
 * Used by Delver of Secrets ("At the beginning of your upkeep, look at the top card of your library.
 * You may reveal that card. If an instant or sorcery card is revealed this way, transform Delver of Secrets.").
 */
public record LookAtTopCardMayRevealTypeTransformEffect(Set<CardType> cardTypes) implements CardEffect {
}
