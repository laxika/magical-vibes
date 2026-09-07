package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayDiscardCardsToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "77")
public class Phantasmagorian extends Card {

    public Phantasmagorian() {
        addEffect(EffectSlot.ON_SELF_CAST, new AnyPlayerMayDiscardCardsToCounterSpellEffect(3));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new DiscardCardTypeCost(null, null, 3),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()),
                "Discard three cards: Return this card from your graveyard to your hand."
        ));
    }
}
