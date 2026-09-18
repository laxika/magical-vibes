package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerMaySearchLibraryForBasicLandToBattlefieldEffect;

@CardRegistration(set = "TMC", collectorNumber = "129")
public class TurtleTracks extends Card {

    public TurtleTracks() {
        target(0, 99).addEffect(EffectSlot.SPELL,
                new EachTargetPlayerMaySearchLibraryForBasicLandToBattlefieldEffect());
    }
}
