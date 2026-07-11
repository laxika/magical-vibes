package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "58")
public class DrownerOfSecrets extends Card {

    public DrownerOfSecrets() {
        // Tap an untapped Merfolk you control: Target player mills a card.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                        new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "Tap an untapped Merfolk you control: Target player mills a card."
        ));
    }
}
