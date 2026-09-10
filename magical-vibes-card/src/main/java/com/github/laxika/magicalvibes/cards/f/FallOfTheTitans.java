package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "109")
public class FallOfTheTitans extends Card {

    public FallOfTheTitans() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{X}{R}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), false));
        target(0, 2).addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new XValue()));
    }
}
