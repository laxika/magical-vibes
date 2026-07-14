package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "7ED", collectorNumber = "174")
public class Bedlam extends Card {

    public Bedlam() {
        // Creatures can't block. (Every creature can't block every creature, board-wide.)
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentIsCreaturePredicate(),
                new PermanentIsCreaturePredicate(),
                "Creatures can't block"));
    }
}
