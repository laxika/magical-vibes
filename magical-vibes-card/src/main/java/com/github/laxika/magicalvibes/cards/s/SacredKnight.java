package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "POR", collectorNumber = "24")
public class SacredKnight extends Card {

    public SacredKnight() {
        // Sacred Knight can't be blocked by black and/or red creatures
        // (i.e. it can only be blocked by creatures that are neither black nor red).
        PermanentNotPredicate notBlackOrRed = new PermanentNotPredicate(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)));
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                notBlackOrRed,
                "creatures that are neither black nor red"
        ));
    }
}
