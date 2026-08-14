package com.github.laxika.magicalvibes.cards.a;

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

@CardRegistration(set = "5DN", collectorNumber = "4")
public class AuriokSalvagers extends Card {

    public AuriokSalvagers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardMaxManaValuePredicate(1)
                        )))
                        .targetGraveyard(true)
                        .build()),
                "{1}{W}: Return target artifact card with mana value 1 or less from your graveyard to your hand."
        ));
    }
}
