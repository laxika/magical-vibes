package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "37")
public class TragicPoet extends Card {

    public TragicPoet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).filter(new CardTypePredicate(CardType.ENCHANTMENT)).build()),
                "{T}, Sacrifice Tragic Poet: Return target enchantment card from your graveyard to your hand."
        ));
    }
}
