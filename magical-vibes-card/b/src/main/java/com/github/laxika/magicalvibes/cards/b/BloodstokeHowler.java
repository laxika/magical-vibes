package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "89")
public class BloodstokeHowler extends Card {

    public BloodstokeHowler() {
        addMorph("{6}{R}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new BoostAllOwnCreaturesEffect(3, 0,
                        new PermanentHasSubtypePredicate(CardSubtype.BEAST)));
    }
}
