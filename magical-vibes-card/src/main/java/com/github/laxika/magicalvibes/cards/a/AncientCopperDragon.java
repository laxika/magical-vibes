package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

@CardRegistration(set = "FCA", collectorNumber = "12")
public class AncientCopperDragon extends Card {

    public AncientCopperDragon() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RollD20Effect(
                CreateTokenEffect.ofTreasureToken(new EventValue()),
                CreateTokenEffect.ofTreasureToken(new EventValue())
        ));
    }
}
