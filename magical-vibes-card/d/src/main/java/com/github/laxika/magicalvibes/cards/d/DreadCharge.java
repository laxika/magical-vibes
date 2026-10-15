package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "POR", collectorNumber = "88")
public class DreadCharge extends Card {

    public DreadCharge() {
        // Black creatures you control can't be blocked this turn except by black creatures.
        addEffect(EffectSlot.SPELL, new GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                "black creatures"
        ));
    }
}
