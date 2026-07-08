package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "11")
public class CribSwap extends Card {

    public CribSwap() {
        // Exile target creature. Its controller creates a 1/1 colorless
        // Shapeshifter creature token with changeling.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect(
                new CreateTokenEffect("Shapeshifter", 1, 1, null,
                        List.of(CardSubtype.SHAPESHIFTER),
                        Set.of(Keyword.CHANGELING), Set.of())
        ));
    }
}
