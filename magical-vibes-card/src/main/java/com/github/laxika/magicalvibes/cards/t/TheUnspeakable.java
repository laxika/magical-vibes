package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "98")
public class TheUnspeakable extends Card {

    public TheUnspeakable() {
        // Whenever The Unspeakable deals combat damage to a player, you may return target
        // Arcane card from your graveyard to your hand. The search-and-choose path is
        // optional, which covers the "you may".
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardSubtypePredicate(CardSubtype.ARCANE))
                .build());
    }
}
