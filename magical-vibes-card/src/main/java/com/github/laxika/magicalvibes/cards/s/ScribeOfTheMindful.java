package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "68")
public class ScribeOfTheMindful extends Card {

    public ScribeOfTheMindful() {
        // {1}, {T}, Sacrifice this creature: Return target instant or sorcery card
        // from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true,  // requiresTap
                "{1}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )))
                        .targetGraveyard(true)
                        .build()),
                "{1}, {T}, Sacrifice this creature: Return target instant or sorcery card from your graveyard to your hand."
        ));
    }
}
