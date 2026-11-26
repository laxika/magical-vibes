package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "MSC", collectorNumber = "685")
public class DaredevilFearlessFighter extends Card {

    public DaredevilFearlessFighter() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(opponent).addTrigger(Triggers.youAreDealtDamageBySourceYouControl(),
                DealDamageEffect.to(Subjects.targetPlayer(), new EventValue()));

        addTrigger(Triggers.attacks(),
                SequenceEffect.of(
                        new ExileTopCardMayPlayThisTurnEffect(false),
                        DealDamageEffect.to(Subjects.you(), AmountThisWay.exiledManaValue())));
    }
}
