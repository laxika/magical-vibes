package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "30")
public class CallToTheKindred extends Card {

    public CallToTheKindred() {
        // You may look at the top five cards of your library. If you do, you may put a creature card that shares a
        // creature type with enchanted creature from among them onto the battlefield, then you put the rest of those
        // cards on the bottom of your library in any order.
        target(TargetFilters.creature())
        .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(
                        LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(5, new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSharesCreatureTypeWithPermanentsPredicate(Subjects.attachedCreature())))),
                        "Look at the top five cards of your library?"
                ));
    }
}
