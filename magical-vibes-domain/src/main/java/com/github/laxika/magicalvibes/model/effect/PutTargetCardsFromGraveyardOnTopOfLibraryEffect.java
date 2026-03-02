package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Put any number of target cards matching the filter from your graveyard on top of your library.
 * Multi-target graveyard selection is handled by SpellCastingService at cast time.
 */
public record PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardPredicate filter) implements CardEffect {
}
