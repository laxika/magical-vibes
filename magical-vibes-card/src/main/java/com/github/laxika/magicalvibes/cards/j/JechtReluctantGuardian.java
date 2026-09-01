package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BraskasFinalAeon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "FIN", collectorNumber = "104")
@CardRegistration(set = "FIN", collectorNumber = "363")
@CardRegistration(set = "FIN", collectorNumber = "448")
public class JechtReluctantGuardian extends Card {

    public JechtReluctantGuardian() {
        setBackFaceCard(new BraskasFinalAeon());

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new ExileSelfAndReturnTransformedEffect(),
                        "Exile Jecht and transform him?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BraskasFinalAeon";
    }
}
