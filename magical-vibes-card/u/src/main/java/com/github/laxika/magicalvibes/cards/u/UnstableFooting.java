package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "ZEN", collectorNumber = "153")
public class UnstableFooting extends Card {

    public UnstableFooting() {
        addStaticEffect(new KickerEffect("{3}{R}"));
        addSpellEffect(new DamageCantBePreventedThisTurnEffect());
        addSpellEffect(new ConditionalEffect(new Kicked(),
                DealDamageEffect.toTargetPlayerOrPlaneswalker(5)));
    }
}
