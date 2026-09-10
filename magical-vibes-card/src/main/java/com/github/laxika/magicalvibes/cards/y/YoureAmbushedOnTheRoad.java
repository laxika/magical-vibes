package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "43")
public class YoureAmbushedOnTheRoad extends Card {

    public YoureAmbushedOnTheRoad() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Make a Retreat — Return target creature you control to its owner's hand",
                        ReturnToHandEffect.target(),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Stand and Fight — Target creature gets +1/+3 until end of turn",
                        new BoostTargetCreatureEffect(1, 3),
                        TargetFilters.creatureYouControl())
        )));
    }
}
