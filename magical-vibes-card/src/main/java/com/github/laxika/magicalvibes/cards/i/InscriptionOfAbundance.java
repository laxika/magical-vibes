package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "186")
public class InscriptionOfAbundance extends Card {

    public InscriptionOfAbundance() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{G}"));

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player gains X life, where X is the greatest power among creatures they control",
                        new TargetPlayerGainsLifeEffect(new GreatestPowerAmongControlled()),
                        anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control fights target creature you don't control",
                        List.of(FightTargetsEffect.forModeTargetFilters()),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()))
        ), new Kicked()));
    }
}
