package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "112")
@CardRegistration(set = "MIC", collectorNumber = "126")
@CardRegistration(set = "SCD", collectorNumber = "97")
@CardRegistration(set = "FDC", collectorNumber = "121")
public class OpenTheGraves extends Card {

    public OpenTheGraves() {
        // Whenever a nontoken creature you control dies, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL), CreateTokenEffect.blackZombie(1));
    }
}
