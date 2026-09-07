package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "159")
public class DowsingShaman extends Card {

    public DowsingShaman() {
        // {2}{G}, {T}: Return target enchantment card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                        .targetGraveyard(true)
                        .build()),
                "{2}{G}, {T}: Return target enchantment card from your graveyard to your hand."
        ));
    }
}
