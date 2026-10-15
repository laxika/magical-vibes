package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "47")
@CardRegistration(set = "BRC", collectorNumber = "112")
public class AudaciousReshapers extends Card {

    public AudaciousReshapers() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect(
                                new CardTypePredicate(CardType.ARTIFACT),
                                LibrarySearchDestination.BATTLEFIELD,
                                false,
                                true),
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CONTROLLER)
                ),
                "{T}, Sacrifice an artifact: Reveal cards from the top of your library until you reveal an artifact card."
                        + " Put that card onto the battlefield and the rest on the bottom of your library in a random order."
                        + " This creature deals damage to you equal to the number of cards revealed this way."
        ));
    }
}
