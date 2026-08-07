package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "159")
@CardRegistration(set = "WTH", collectorNumber = "115")
public class Thunderbolt extends Card {

    public Thunderbolt() {
        var playerOrPlaneswalkerFilter = new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker.");
        var creatureWithFlyingFilter = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                )),
                "Target must be a creature with flying.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Thunderbolt deals 3 damage to target player or planeswalker",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(3),
                        playerOrPlaneswalkerFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Thunderbolt deals 4 damage to target creature with flying",
                        new DealDamageToTargetCreatureEffect(4),
                        creatureWithFlyingFilter)
        )));
    }
}
