package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "174")
public class MultaniYavimayasAvatar extends Card {

    public MultaniYavimayasAvatar() {
        // Reach, trample — auto-loaded from Scryfall

        // Multani, Yavimaya's Avatar gets +1/+1 for each land you control
        PermanentCount landsYouControl =
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(landsYouControl, landsYouControl));

        // ... and each land card in your graveyard.
        CardsInGraveyard landCardsInGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(landCardsInGraveyard, landCardsInGraveyard));

        // {1}{G}, Return two lands you control to their owner's hand:
        // Return Multani, Yavimaya's Avatar from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(2, new PermanentIsLandPredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{1}{G}, Return two lands you control to their owner's hand: Return Multani, Yavimaya's Avatar from your graveyard to your hand."
        ));
    }
}
