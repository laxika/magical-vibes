package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "INR", collectorNumber = "95")
public class ArchghoulOfThraben extends Card {

    private static final LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect LOOK =
            new LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect(
                    new CardSubtypePredicate(CardSubtype.ZOMBIE));

    public ArchghoulOfThraben() {
        // Whenever this creature or another Zombie you control dies, look at the top card of your
        // library. If it's a Zombie card, you may reveal it and put it into your hand. If you don't
        // put the card into your hand, you may put it into your graveyard.
        //
        // Ally-death watchers are already off the battlefield when collected, so ON_ALLY alone is
        // "another Zombie"; ON_DEATH covers this creature's own death (Arnyn Deathbloom Botanist).
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE), LOOK));
        addEffect(EffectSlot.ON_DEATH, LOOK);
    }
}
