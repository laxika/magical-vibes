package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandTappedForManaEffect;

@CardRegistration(set = "TMT", collectorNumber = "115")
@CardRegistration(set = "TMT", collectorNumber = "238")
public class GroundchuckDirtbag extends Card {

    public GroundchuckDirtbag() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaWhenLandTappedForManaEffect(ManaColor.GREEN));
    }
}
