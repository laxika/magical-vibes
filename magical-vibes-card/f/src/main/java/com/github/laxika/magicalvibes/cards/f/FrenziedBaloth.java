package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;

import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "183")
public class FrenziedBaloth extends Card {

    public FrenziedBaloth() {
        addStaticEffect(new CantBeCounteredEffect());
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(Set.of(CardType.CREATURE)));
        addStaticEffect(DamageCantBePreventedEffect.combatDamage());
    }
}
