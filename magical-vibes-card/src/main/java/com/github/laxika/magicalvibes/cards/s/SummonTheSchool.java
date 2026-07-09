package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "42")
public class SummonTheSchool extends Card {

    public SummonTheSchool() {
        // Create two 1/1 blue Merfolk Wizard creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Merfolk Wizard", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.MERFOLK, CardSubtype.WIZARD),
                Set.of(), Set.of()));

        // Tap four untapped Merfolk you control: Return this card from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(4, new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()),
                "Tap four untapped Merfolk you control: Return Summon the School from your graveyard to your hand."
        ));
    }
}
