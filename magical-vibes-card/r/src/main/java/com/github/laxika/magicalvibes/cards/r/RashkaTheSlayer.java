package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "12")
public class RashkaTheSlayer extends Card {

    public RashkaTheSlayer() {
        // Whenever Rashka blocks one or more black creatures, Rashka gets +1/+2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenCombatOpponentMatchesEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)), 1, 2),
                TriggerMode.ONCE_PER_BLOCK);
    }
}
