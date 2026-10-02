package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToGraveyardEffect;

@CardRegistration(set = "YBRO", collectorNumber = "27")
public class GixianRecycler extends Card {

    public GixianRecycler() {
        ConjureCardToGraveyardEffect conjureDuplicate =
                new ConjureCardToGraveyardEffect("YBRO", "27");
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, conjureDuplicate);
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_HAND, conjureDuplicate);
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_LIBRARY, conjureDuplicate);

        addUnearth("{1}{B}");
    }
}
