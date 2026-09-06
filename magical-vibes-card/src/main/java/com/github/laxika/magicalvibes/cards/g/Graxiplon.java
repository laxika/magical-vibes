package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedUnlessDefenderControlsCreaturesSharingCreatureTypeEffect;

@CardRegistration(set = "ONS", collectorNumber = "86")
public class Graxiplon extends Card {

    public Graxiplon() {
        addEffect(EffectSlot.STATIC,
                new CantBeBlockedUnlessDefenderControlsCreaturesSharingCreatureTypeEffect(3));
    }
}
