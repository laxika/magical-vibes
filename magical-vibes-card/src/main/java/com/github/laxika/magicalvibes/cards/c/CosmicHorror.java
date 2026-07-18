package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroySourceAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "126")
public class CosmicHorror extends Card {

    public CosmicHorror() {
        // At the beginning of your upkeep, destroy this creature unless you pay {3}{B}{B}{B}.
        // If this creature is destroyed this way, it deals 7 damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{3}{B}{B}{B}"),
                        List.of(new DestroySourceAndDamageControllerIfDestroyedEffect(7)),
                        true));
    }
}
