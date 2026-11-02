package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "118")
@CardRegistration(set = "SLD", collectorNumber = "844")
@CardRegistration(set = "DBL", collectorNumber = "385")
@CardRegistration(set = "FDC", collectorNumber = "106")
public class HeadlessRider extends Card {

    private static final CreateTokenEffect ZOMBIE_TOKEN = CreateTokenEffect.blackZombie(1);

    public HeadlessRider() {
        // Whenever this creature or another nontoken Zombie you control dies, create a 2/2 black
        // Zombie creature token.
        addTrigger(Triggers.nontokenCreatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                ZOMBIE_TOKEN);
    }
}
