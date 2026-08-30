package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "7")
public class AirbendersReversal extends Card {

    public AirbendersReversal() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target attacking creature",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.attackingCreature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Airbend target creature you control",
                        new AirbendTargetPermanentEffect(),
                        TargetFilters.creatureYouControl())
        )));
    }
}
