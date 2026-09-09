package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "1")
@CardRegistration(set = "ATQ", collectorNumber = "94")
@CardRegistration(set = "ME1", collectorNumber = "3")
public class ArgivianArchaeologist extends Card {

    public ArgivianArchaeologist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.ARTIFACT))
                        .targetGraveyard(true)
                        .build()),
                "{W}{W}, {T}: Return target artifact card from your graveyard to your hand."
        ));
    }
}
