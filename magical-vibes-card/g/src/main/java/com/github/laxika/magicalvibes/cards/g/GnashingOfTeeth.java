package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "69")
public class GnashingOfTeeth extends Card {

    public GnashingOfTeeth() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -5/-5 until end of turn. If that creature would die this turn, exile it instead",
                        SequenceEffect.of(
                                new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(),
                                BoostEffect.target(-5, -5)),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures target player controls get -1/-1 until end of turn",
                        BoostEffect.creaturesTargetPlayerControls(-1, -1),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."))
        )));
    }
}
