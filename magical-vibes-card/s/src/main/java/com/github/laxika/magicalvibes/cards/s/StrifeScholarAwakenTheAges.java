package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AwakenTheAges;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Strife Scholar // Awaken the Ages (SOS 131). */
@CardRegistration(set = "SOS", collectorNumber = "131")
public class StrifeScholarAwakenTheAges extends Card {

    public StrifeScholarAwakenTheAges() {
        setBackFaceCard(new AwakenTheAges());
        addStaticEffect(new BecomePreparedEffect());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysLifeEffect(new Fixed(2)));
    }

    @Override
    public String getBackFaceClassName() {
        return "AwakenTheAges";
    }
}
