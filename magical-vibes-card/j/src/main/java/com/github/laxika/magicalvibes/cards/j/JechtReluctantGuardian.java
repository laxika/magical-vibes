package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BraskasFinalAeon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "104")
@CardRegistration(set = "FIN", collectorNumber = "363")
@CardRegistration(set = "FIN", collectorNumber = "448")
public class JechtReluctantGuardian extends Card {

    public JechtReluctantGuardian() {
        setBackFaceCard(new BraskasFinalAeon());

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new ExileSelfAndReturnTransformedEffect(),
                        "Exile Jecht and transform him?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BraskasFinalAeon";
    }
}
