package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "1")
@CardRegistration(set = "VOC", collectorNumber = "39")
public class MillicentRestlessRevenant extends Card {

    private static final CreateTokenEffect SPIRIT_TOKEN = CreateTokenEffect.whiteSpirit(1);

    public MillicentRestlessRevenant() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT), CountScope.CONTROLLER)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SPIRIT_TOKEN);

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )), SPIRIT_TOKEN));

        addEffect(EffectSlot.ON_DEATH, SPIRIT_TOKEN);
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SPIRIT), SPIRIT_TOKEN));
    }
}
