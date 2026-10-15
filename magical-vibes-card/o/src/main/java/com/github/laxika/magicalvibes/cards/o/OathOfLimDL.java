package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "156")
public class OathOfLimDL extends Card {

    public OathOfLimDL() {
        // Whenever you lose life, for each 1 life you lost, sacrifice a permanent other than this
        // enchantment unless you discard a card.
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE,
                new SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect());

        // {B}{B}: Draw a card.
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}",
                List.of(new DrawCardEffect(1)),
                "{B}{B}: Draw a card."));
    }
}
