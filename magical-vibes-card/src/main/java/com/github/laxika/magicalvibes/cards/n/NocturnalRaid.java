package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "7ED", collectorNumber = "151")
public class NocturnalRaid extends Card {

    public NocturnalRaid() {
        // Black creatures get +2/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0,
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
    }
}
