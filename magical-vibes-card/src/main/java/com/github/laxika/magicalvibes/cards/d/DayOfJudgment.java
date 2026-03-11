package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "12")
public class DayOfJudgment extends Card {

    public DayOfJudgment() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(Set.of(CardType.CREATURE), false));
    }
}
