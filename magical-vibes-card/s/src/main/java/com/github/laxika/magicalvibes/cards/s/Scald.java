package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "USG", collectorNumber = "211")
public class Scald extends Card {

    public Scald() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new DealDamageOnLandTapEffect(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
