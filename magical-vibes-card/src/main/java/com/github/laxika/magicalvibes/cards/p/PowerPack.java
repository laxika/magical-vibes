package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect;

@CardRegistration(set = "MSC", collectorNumber = "90")
@CardRegistration(set = "MSC", collectorNumber = "411")
public class PowerPack extends Card {

    public PowerPack() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect());
    }
}
