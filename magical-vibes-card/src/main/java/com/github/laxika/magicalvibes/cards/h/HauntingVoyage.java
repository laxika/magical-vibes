package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.condition.CastForForetellCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesOfChosenTypeFromGraveyardEffect;

@CardRegistration(set = "KHM", collectorNumber = "98")
public class HauntingVoyage extends Card {

    public HauntingVoyage() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastForForetellCost(),
                new ReturnCreaturesOfChosenTypeFromGraveyardEffect(2),
                new ReturnCreaturesOfChosenTypeFromGraveyardEffect()));

        addCastingOption(new ForetellCast("{5}{B}{B}"));
    }
}
