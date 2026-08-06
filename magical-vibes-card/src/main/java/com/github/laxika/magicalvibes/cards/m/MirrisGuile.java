package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

@CardRegistration(set = "TMP", collectorNumber = "236")
public class MirrisGuile extends Card {

    public MirrisGuile() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ReorderTopCardsOfLibraryEffect(3),
                "Look at the top three cards of your library and put them back in any order?"
        ));
    }
}
