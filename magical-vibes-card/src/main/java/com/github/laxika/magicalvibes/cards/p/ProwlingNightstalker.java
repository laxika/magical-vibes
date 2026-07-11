package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "P02", collectorNumber = "83")
public class ProwlingNightstalker extends Card {

    public ProwlingNightstalker() {
        // Prowling Nightstalker can't be blocked except by black creatures.
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                "black creatures"
        ));
    }
}
