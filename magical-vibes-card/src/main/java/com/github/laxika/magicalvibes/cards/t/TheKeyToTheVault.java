package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayCastExiledNonlandCardEffect;

@CardRegistration(set = "OTJ", collectorNumber = "54")
public class TheKeyToTheVault extends Card {

    public TheKeyToTheVault() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LookAtTopCardsMayCastExiledNonlandCardEffect(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}{U}"));
    }
}
