package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "156")
public class RazeTheEffigy extends Card {

    public RazeTheEffigy() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target attacking creature gets +2/+2 until end of turn",
                        new BoostTargetCreatureEffect(2, 2),
                        TargetFilters.attackingCreature())
        )));
    }
}
