package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BOK", collectorNumber = "85")
public class StirTheGrave extends Card {

    public StirTheGrave() {
        // Intrinsic graveyard targeting: the creature card is chosen at cast time and
        // restricted to mana value <= the paid X.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .targetGraveyard(true)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .requiresManaValueAtMostX(true)
                .build());
    }
}
