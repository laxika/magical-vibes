package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M3C", collectorNumber = "48")
@CardRegistration(set = "M3C", collectorNumber = "100")
public class MarchFromVelisVel extends Card {

    public MarchFromVelisVel() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL,
                        new EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect());
        addCastingOption(new FlashbackCast("{4}{U}"));
    }
}
