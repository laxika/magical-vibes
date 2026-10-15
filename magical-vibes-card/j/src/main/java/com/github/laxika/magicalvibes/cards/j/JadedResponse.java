package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetSpellSharesColorWithControlledCreature;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "APC", collectorNumber = "26")
public class JadedResponse extends Card {

    public JadedResponse() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetSpellSharesColorWithControlledCreature(),
                new CounterSpellEffect()));
    }
}
