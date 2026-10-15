package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "46")
public class PolymorphousRush extends Card {

    public PolymorphousRush() {
        setAdditionalManaCostPerExtraTarget("{1}{U}");
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addEffect(EffectSlot.SPELL, new MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect());
    }
}
