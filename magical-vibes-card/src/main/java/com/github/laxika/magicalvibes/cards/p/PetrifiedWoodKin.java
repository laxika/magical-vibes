package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "91")
public class PetrifiedWoodKin extends Card {

    public PetrifiedWoodKin() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new DamageDealtToOpponentsThisTurn()));
        addEffect(EffectSlot.STATIC, new ProtectionFromCardTypesEffect(Set.of(CardType.INSTANT)));
    }
}
