package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "30")
public class RamosianRevivalist extends Card {

    public RamosianRevivalist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.REBEL),
                                new CardIsPermanentPredicate(),
                                new CardMaxManaValuePredicate(5))))
                        .targetGraveyard(true)
                        .build()),
                "{6}, {T}: Return target Rebel permanent card with mana value 5 or less from your graveyard to the battlefield."
        ));
    }
}
