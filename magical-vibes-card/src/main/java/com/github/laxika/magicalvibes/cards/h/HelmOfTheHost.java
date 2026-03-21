package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;

@CardRegistration(set = "DOM", collectorNumber = "217")
public class HelmOfTheHost extends Card {

    public HelmOfTheHost() {
        // At the beginning of combat on your turn, create a token that's a copy of
        // equipped creature, except the token isn't legendary. That token gains haste.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new CreateTokenCopyOfEquippedCreatureEffect(true, true));

        // Equip {5}
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
