package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "48")
public class SecretsOfTheDead extends Card {

    public SecretsOfTheDead() {
        // Whenever you cast a spell from your graveyard, draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CastFromGraveyardTriggerEffect(
                List.of(new DrawCardEffect(1))
        ));
    }
}
