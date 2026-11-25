package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "SOS", collectorNumber = "4")
public class TogetherAsOne extends Card {

    public TogetherAsOne() {
        // Each "target" is a separate instance, so one player may be chosen for both (CR 115.3).
        setAllowSharedTargets(true);

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new DrawCardEffect(new XValue(), Subjects.targetPlayer()));
        target(new AnyTargetPredicateTargetFilter(
                new PermanentTruePredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a legal damage target")).addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()));
        addSpellEffect(new GainLifeEffect(new XValue()));
    }
}
