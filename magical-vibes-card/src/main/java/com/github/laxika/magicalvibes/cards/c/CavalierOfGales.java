package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "M20", collectorNumber = "52")
public class CavalierOfGales extends Card {

    public CavalierOfGales() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(3, 2, HandToLibraryPlacement.TOP));
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ShuffleSelfFromGraveyardIntoLibraryEffect(),
                new ScryEffect(2)));
    }
}
