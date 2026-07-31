package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "128")
public class ScarabOfTheUnseen extends Card {

    public ScarabOfTheUnseen() {
        // {T}, Sacrifice this artifact: Return all Auras attached to target permanent you own to
        // their owners' hands. Draw a card at the beginning of the next turn's upkeep.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        ReturnToHandEffect.aurasAttachedToTarget(),
                        new RegisterDrawCardsAtNextUpkeepEffect()
                ),
                "{T}, Sacrifice this artifact: Return all Auras attached to target permanent you own to their owners' hands. Draw a card at the beginning of the next turn's upkeep.",
                new OwnedPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you own"
                )
        ));
    }
}
