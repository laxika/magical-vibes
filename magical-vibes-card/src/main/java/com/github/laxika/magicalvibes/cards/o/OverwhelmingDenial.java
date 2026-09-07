package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "61")
public class OverwhelmingDenial extends Card {

    public OverwhelmingDenial() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{U}{U}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()),
                false));
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
