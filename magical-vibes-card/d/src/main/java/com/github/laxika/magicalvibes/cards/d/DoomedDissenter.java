package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "87")
@CardRegistration(set = "M19", collectorNumber = "93")
@CardRegistration(set = "AKR", collectorNumber = "101")
@CardRegistration(set = "VOW", collectorNumber = "106")
@CardRegistration(set = "GN3", collectorNumber = "48")
@CardRegistration(set = "DBL", collectorNumber = "373")
public class DoomedDissenter extends Card {

    public DoomedDissenter() {
        // When Doomed Dissenter dies, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.blackZombie(1));
    }
}
