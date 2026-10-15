package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "4")
public class PathsOfTuinvale extends Card {

    public PathsOfTuinvale() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature you control to its owner's hand. That card perpetually costs {1} less to cast",
                        ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect.reduceCost(1),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature you don't control to its owner's hand. That card perpetually costs {1} more to cast",
                        ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect.increaseCost(1),
                        TargetFilters.creatureAnOpponentControls())
        )));
    }
}
