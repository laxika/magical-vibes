package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "313")
public class SkullOfOrm extends Card {

    public SkullOfOrm() {
        // {5}, {T}: Return target enchantment card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true, "{5}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                        .targetGraveyard(true)
                        .build()),
                "{5}, {T}: Return target enchantment card from your graveyard to your hand."
        ));
    }
}
