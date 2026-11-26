package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;


@CardRegistration(set = "NEO", collectorNumber = "101")
public class InvokeDespair extends Card {

    public InvokeDespair() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        target(opponent)
                .addSpellEffect(sacrificeOrElse(new PermanentIsCreaturePredicate()))
                .addSpellEffect(sacrificeOrElse(new PermanentIsEnchantmentPredicate()))
                .addSpellEffect(sacrificeOrElse(new PermanentIsPlaneswalkerPredicate()));
    }

    /** "Target opponent sacrifices [a filter permanent] of their choice. If they can't, they lose 2 life and you draw a card." */
    private CardEffect sacrificeOrElse(PermanentPredicate filter) {
        return SequenceEffect.of(
                new SacrificePermanentsEffect(1, filter, Subjects.targetPlayer()),
                new ConditionalEffect(new NotCondition(HappenedThisWay.sacrificed()),
                        SequenceEffect.of(new LoseLifeEffect(2, Subjects.targetPlayer()), new DrawCardEffect(1))));
    }
}
