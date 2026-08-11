package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsCantBeTargetedEffect;

@CardRegistration(set = "M13", collectorNumber = "176")
@CardRegistration(set = "ODY", collectorNumber = "242")
public class GroundSeal extends Card {

    public GroundSeal() {
        // When this enchantment enters, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

        // Cards in graveyards can't be the targets of spells or abilities.
        addEffect(EffectSlot.STATIC, new GraveyardCardsCantBeTargetedEffect());
    }
}
