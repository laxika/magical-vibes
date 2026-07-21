package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

/**
 * Cipherbound Spirit — back face of Soulcipher Board // Cipherbound Spirit.
 * Creature — Spirit 3/2
 * Flying
 * This creature can block only creatures with flying.
 * {3}{U}: Draw two cards, then discard a card.
 */
public class CipherboundSpirit extends Card {

    public CipherboundSpirit() {
        // Flying is auto-loaded from Scryfall.

        // This creature can block only creatures with flying.
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                "creatures with flying"
        ));

        // {3}{U}: Draw two cards, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new DrawCardEffect(2), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{3}{U}: Draw two cards, then discard a card."
        ));
    }
}
