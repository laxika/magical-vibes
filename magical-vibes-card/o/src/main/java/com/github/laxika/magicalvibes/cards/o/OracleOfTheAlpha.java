package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjurePowerNineIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "259")
@CardRegistration(set = "YDMU", collectorNumber = "4")
public class OracleOfTheAlpha extends Card {

    public OracleOfTheAlpha() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConjurePowerNineIntoLibraryEffect());
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
