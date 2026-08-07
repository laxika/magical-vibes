package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "211")
public class HanaKami extends Card {

    public HanaKami() {
        // "{1}{G}, Sacrifice this creature: Return target Arcane card from your graveyard to your hand."
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{G}",
                List.of(new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardSubtypePredicate(CardSubtype.ARCANE))
                                .targetGraveyard(true)
                                .build()),
                "{1}{G}, Sacrifice this creature: Return target Arcane card from your graveyard to your hand."
        ));
    }
}
