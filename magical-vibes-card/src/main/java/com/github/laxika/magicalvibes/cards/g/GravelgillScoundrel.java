package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "53")
public class GravelgillScoundrel extends Card {

    public GravelgillScoundrel() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayTapPermanentsEffect(
                new TapMultiplePermanentsCost(1,
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK), true),
                new MakeCreatureUnblockableEffect(true),
                "Tap another untapped Merfolk you control?"));
    }
}
