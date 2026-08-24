package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandPermanentAndCardWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SPM", collectorNumber = "153")
public class TheSpotLivingPortal extends Card {

    public TheSpotLivingPortal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetNonlandPermanentAndCardWithSourceEffect());
        addEffect(EffectSlot.ON_DEATH,
                new PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect());
    }
}
