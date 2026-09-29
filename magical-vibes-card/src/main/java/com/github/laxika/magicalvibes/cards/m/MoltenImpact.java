package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOneShotInstantSorcerySpellDamageBoonEffect;

@CardRegistration(set = "YNEO", collectorNumber = "22")
public class MoltenImpact extends Card {

    public MoltenImpact() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new EventValueAtLeast(1),
                new RegisterOneShotInstantSorcerySpellDamageBoonEffect()));
    }
}
