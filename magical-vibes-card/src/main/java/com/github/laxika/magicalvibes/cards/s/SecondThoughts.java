package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "45")
public class SecondThoughts extends Card {

    public SecondThoughts() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
