package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "9ED", collectorNumber = "173")
public class Anarchist extends Card {

    public Anarchist() {
        // When this creature enters, you may return target sorcery card from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.SORCERY))
                        .build(),
                "Return a sorcery card from your graveyard to your hand?"
        ));
    }
}
