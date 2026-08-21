package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEnteringCreatureHasUniqueNameEffect;

@CardRegistration(set = "RNA", collectorNumber = "130")
public class GuardianProject extends Card {

    public GuardianProject() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new DrawCardIfEnteringCreatureHasUniqueNameEffect());
    }
}
