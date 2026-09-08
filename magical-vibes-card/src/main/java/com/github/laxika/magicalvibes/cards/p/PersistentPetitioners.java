package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "44")
public class PersistentPetitioners extends Card {

    public PersistentPetitioners() {
        // {1}, {T}: Target player mills a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "{1}, {T}: Target player mills a card."
        ));

        // Tap four untapped Advisors you control: Target player mills twelve cards.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(4,
                                new PermanentHasSubtypePredicate(CardSubtype.ADVISOR)),
                        new MillEffect(12, MillRecipient.TARGET_PLAYER)
                ),
                "Tap four untapped Advisors you control: Target player mills twelve cards."
        ));
    }
}
