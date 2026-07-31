package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "220")
@CardRegistration(set = "M14", collectorNumber = "225")
public class TradingPost extends Card {

    public TradingPost() {
        // {1}, {T}, Discard a card: You gain 4 life.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new DiscardCardTypeCost(null, null), new GainLifeEffect(4)),
                "{1}, {T}, Discard a card: You gain 4 life."
        ));

        // {1}, {T}, Pay 1 life: Create a 0/1 white Goat creature token.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new PayLifeCost(1), new CreateTokenEffect("Goat", 0, 1, CardColor.WHITE,
                        List.of(CardSubtype.GOAT), Set.of(), Set.of())),
                "{1}, {T}, Pay 1 life: Create a 0/1 white Goat creature token."
        ));

        // {1}, {T}, Sacrifice a creature: Return target artifact card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.ARTIFACT))
                                .targetGraveyard(true)
                                .build()
                ),
                "{1}, {T}, Sacrifice a creature: Return target artifact card from your graveyard to your hand."
        ));

        // {1}, {T}, Sacrifice an artifact: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new SacrificeArtifactCost(), new DrawCardEffect(1)),
                "{1}, {T}, Sacrifice an artifact: Draw a card."
        ));
    }
}
