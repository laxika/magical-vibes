package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "84")
@CardRegistration(set = "NCC", collectorNumber = "184")
public class SmugglersBuggy extends Card {

    public SmugglersBuggy() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, and put
        // the rest on the bottom of your library in a random order.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ImprintFromTopCardsEffect(4, true));

        // Whenever this Vehicle deals combat damage to a player, you may cast the exiled card
        // without paying its mana cost. If you do, return this Vehicle to its owner's hand.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsSourcePermanentPredicate()),
                MayCastCardExiledWithSourceEffect.returningSourceToHandIfCast());

        // Crew 2.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
