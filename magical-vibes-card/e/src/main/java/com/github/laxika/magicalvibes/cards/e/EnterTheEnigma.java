package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "52")
public class EnterTheEnigma extends Card {

    public EnterTheEnigma() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
