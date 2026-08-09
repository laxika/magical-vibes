package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.HaveFullTextOfTopCreatureCardInGraveyardEffect;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "48")
public class VolrathsShapeshifter extends Card {

    public VolrathsShapeshifter() {
        addEffect(EffectSlot.STATIC, new HaveFullTextOfTopCreatureCardInGraveyardEffect());
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{2}: Discard a card."));
    }
}
