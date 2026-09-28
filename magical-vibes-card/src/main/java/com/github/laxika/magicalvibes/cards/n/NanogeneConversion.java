package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "49")
@CardRegistration(set = "WHO", collectorNumber = "364")
public class NanogeneConversion extends Card {

    public NanogeneConversion() {
        // Choose target creature you control. Each other creature becomes a copy of that creature until end of turn, except it isn't legendary.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL,
                        new EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect(true));
    }
}
