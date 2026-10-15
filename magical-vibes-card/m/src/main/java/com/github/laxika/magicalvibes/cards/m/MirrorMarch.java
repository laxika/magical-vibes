package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;

@CardRegistration(set = "RNA", collectorNumber = "108")
public class MirrorMarch extends Card {

    public MirrorMarch() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new FlipUntilLoseEffect(new CreateTokenCopyOfTargetPermanentEffect(true, true)));
    }
}
