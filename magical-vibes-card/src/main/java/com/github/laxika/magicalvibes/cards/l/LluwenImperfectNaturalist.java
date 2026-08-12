package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "232")
@CardRegistration(set = "ECL", collectorNumber = "375")
public class LluwenImperfectNaturalist extends Card {

    public LluwenImperfectNaturalist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect(4));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B/G}{B/G}{B/G}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER),
                                "Worm", 1, 1, CardColor.BLACK,
                                Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.WORM),
                                Set.of(), Set.of(), false, false, java.util.Map.of(), List.of(),
                                false, false, false, 0, Set.of())
                ),
                "{2}{B/G}{B/G}{B/G}, {T}, Discard a land card: Create a 1/1 black and green Worm creature token for each land card in your graveyard."
        ));
    }
}
