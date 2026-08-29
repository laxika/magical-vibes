package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "87")
public class RazorjawOni extends Card {

    public RazorjawOni() {
        // Black creatures can't block.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                new PermanentIsCreaturePredicate(),
                "Black creatures can't block"));
    }
}
