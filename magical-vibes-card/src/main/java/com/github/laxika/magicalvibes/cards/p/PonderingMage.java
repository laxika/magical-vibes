package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "63")
public class PonderingMage extends Card {

    public PonderingMage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SequenceEffect(List.of(
                new ReorderTopCardsOfLibraryEffect(3),
                new MayEffect(new ShuffleLibraryEffect(false), "You may shuffle your library."),
                new DrawCardEffect(1))));
    }
}
