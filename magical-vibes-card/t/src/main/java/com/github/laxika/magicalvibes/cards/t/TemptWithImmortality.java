package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferReturnCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "C13", collectorNumber = "95")
public class TemptWithImmortality extends Card {

    public TemptWithImmortality() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(creature)
                .mandatory(true)
                .build());
        addSpellEffect(new TemptingOfferReturnCreatureFromGraveyardEffect());
    }
}
