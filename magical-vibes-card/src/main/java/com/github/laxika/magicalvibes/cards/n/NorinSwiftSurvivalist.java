package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DSK", collectorNumber = "145")
public class NorinSwiftSurvivalist extends Card {

    public NorinSwiftSurvivalist() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED,
                new MayEffect(new ExileTriggeringCreatureMayPlayThisTurnEffect(),
                        "Exile that creature and play it this turn?"));
    }
}
