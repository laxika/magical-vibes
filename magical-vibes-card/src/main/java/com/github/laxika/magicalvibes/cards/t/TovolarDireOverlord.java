package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeNightEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "246")
public class TovolarDireOverlord extends Card {

    public TovolarDireOverlord() {
        setBackFaceCard(new TovolarTheMidnightScourge());

        PermanentPredicate wolfOrWerewolf = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(wolfOrWerewolf, new DrawCardEffect(1)));

        PermanentPredicate humanWerewolf = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(3, wolfOrWerewolf),
                SequenceEffect.of(
                        new BecomeNightEffect(),
                        new TransformAnyNumberOfPermanentsEffect(humanWerewolf))));
    }

    @Override
    public String getBackFaceClassName() {
        return "TovolarTheMidnightScourge";
    }
}
