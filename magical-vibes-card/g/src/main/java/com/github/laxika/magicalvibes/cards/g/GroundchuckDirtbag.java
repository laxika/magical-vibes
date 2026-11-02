package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandTappedForManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "115")
@CardRegistration(set = "TMT", collectorNumber = "238")
public class GroundchuckDirtbag extends Card {

    public GroundchuckDirtbag() {
        addTrigger(Triggers.playerTapsLandForMana(),
                new AddManaWhenLandTappedForManaEffect(ManaColor.GREEN));
    }
}
