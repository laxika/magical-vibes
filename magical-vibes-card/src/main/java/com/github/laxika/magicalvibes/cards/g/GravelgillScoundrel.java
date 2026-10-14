package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ECL", collectorNumber = "53")
public class GravelgillScoundrel extends Card {

    public GravelgillScoundrel() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayTapPermanentsEffect(
                new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate(), true),
                new MakeCreatureUnblockableEffect(true),
                "Tap another untapped creature you control?"));
    }
}
