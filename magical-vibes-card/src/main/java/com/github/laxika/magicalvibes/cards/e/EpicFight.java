package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "166")
public class EpicFight extends Card {

    public EpicFight() {
        setAllowSharedTargets(true);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Double target creature's power and toughness until end of turn",
                        new DoubleSelfPowerToughnessEffect(),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature an opponent controls",
                        List.of(FightTargetsEffect.boundTargetGroupAndNext()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls())
                )
        )));
    }
}
