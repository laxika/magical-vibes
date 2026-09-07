package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "9")
public class FearOfAbduction extends Card {

    public FearOfAbduction() {
        addEffect(EffectSlot.SPELL, new ExileCreatureCost());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentAndTrackWithSourceEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
