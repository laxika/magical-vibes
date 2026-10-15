package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "PCY", collectorNumber = "77")
public class RhysticTutor extends Card {

    public RhysticTutor() {
        addEffect(EffectSlot.SPELL, new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2}?",
                MayPayPayer.ANY_PLAYER,
                new SearchLibraryEffect(),
                0));
    }
}
