package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "31")
@CardRegistration(set = "HOC", collectorNumber = "71")
public class FlameOfAnor extends Card {

    public FlameOfAnor() {
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");

        var options = List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws two cards",
                        new DrawCardForTargetPlayerEffect(2), anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(), TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Flame of Anor deals 5 damage to target creature",
                        new DealDamageToTargetCreatureEffect(5), TargetFilters.creature())
        );

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(options, false, 1, 2, false,
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.WIZARD))));
    }
}
