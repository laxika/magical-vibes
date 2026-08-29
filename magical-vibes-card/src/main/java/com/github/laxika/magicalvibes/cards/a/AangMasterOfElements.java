package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

public class AangMasterOfElements extends Card {

    public AangMasterOfElements() {
        addEffect(EffectSlot.STATIC, new ReduceColoredCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), new ManaCost("{W}{U}{B}{R}{G}"),
                CostModificationScope.SELF, true));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new TransformSelfThenEffect(List.of(
                        new GainLifeEffect(4),
                        new DrawCardEffect(4),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 4),
                        new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT))),
                "Transform Aang, Master of Elements?"));
    }
}
