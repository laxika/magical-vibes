package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "58")
public class ImpelledGiant extends Card {

    public ImpelledGiant() {
        // Trample is auto-loaded from Scryfall.

        // Tap an untapped red creature you control other than this creature: This creature gets
        // +X/+0 until end of turn, where X is the power of the creature tapped this way.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapCreatureCost(new PermanentColorInPredicate(Set.of(CardColor.RED)), true, true),
                        new BoostSelfEffect(new ChosenPermanentPower(), new Fixed(0))),
                "Tap an untapped red creature you control other than this creature: This creature "
                        + "gets +X/+0 until end of turn, where X is the power of the creature tapped this way."
        ));
    }
}
