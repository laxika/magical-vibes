package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "1")
@CardRegistration(set = "VOC", collectorNumber = "39")
public class MillicentRestlessRevenant extends Card {

    private static final CreateTokenEffect SPIRIT_TOKEN = CreateTokenEffect.whiteSpirit(1);

    public MillicentRestlessRevenant() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT), CountScope.CONTROLLER)));

        addTrigger(Triggers.dealsCombatDamageToPlayer(), SPIRIT_TOKEN);

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )), SPIRIT_TOKEN));

        addTrigger(Triggers.nontokenCreatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                SPIRIT_TOKEN);
    }
}
