package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "95")
@CardRegistration(set = "INR", collectorNumber = "370")
@CardRegistration(set = "VOW", collectorNumber = "93")
@CardRegistration(set = "DBL", collectorNumber = "360")
public class ArchghoulOfThraben extends Card {

    private static final LookAtTopCardMayRevealMatchingToHandEffect LOOK =
            new LookAtTopCardMayRevealMatchingToHandEffect(
                    new CardSubtypePredicate(CardSubtype.ZOMBIE), true);

    public ArchghoulOfThraben() {
        // Whenever this creature or another Zombie you control dies, look at the top card of your
        // library. If it's a Zombie card, you may reveal it and put it into your hand. If you don't
        // put the card into your hand, you may put it into your graveyard.
        //
        // "This creature" counts whatever its types are, so it is its own "when this dies" ability and the
        // Zombie restriction applies only to another creature.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                LOOK);
    }
}
