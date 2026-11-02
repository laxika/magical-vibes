package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "145")
public class NorinSwiftSurvivalist extends Card {

    public NorinSwiftSurvivalist() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.creatureYouControlBecomesBlocked(),
                new MayEffect(new ExileTriggeringCreatureMayPlayThisTurnEffect(),
                        "Exile that creature and play it this turn?"));
    }
}
