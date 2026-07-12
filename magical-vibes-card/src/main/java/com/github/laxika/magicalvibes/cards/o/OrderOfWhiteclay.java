package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "16")
public class OrderOfWhiteclay extends Card {

    public OrderOfWhiteclay() {
        // {1}{W}{W}, {Q}: Return target creature card with mana value 3 or less from your graveyard to the battlefield.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{W}{W}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3))))
                        .targetGraveyard(true)
                        .build()),
                "{1}{W}{W}, {Q}: Return target creature card with mana value 3 or less from your graveyard to the battlefield."
        ).withRequiresUntap());
    }
}
