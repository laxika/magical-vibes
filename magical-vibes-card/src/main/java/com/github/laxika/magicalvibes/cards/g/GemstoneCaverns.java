package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GemstoneCavernsStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "274")
public class GemstoneCaverns extends Card {

    public GemstoneCaverns() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new GemstoneCavernsStartOnBattlefieldEffect(),
                "Begin the game with Gemstone Caverns on the battlefield?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LUCK)),
                "Activate only if Gemstone Caverns has no luck counters on it."));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ).withActivationCondition(
                new SourceCounterThreshold(1, CounterType.LUCK),
                "Activate only if Gemstone Caverns has a luck counter on it."));
    }
}
