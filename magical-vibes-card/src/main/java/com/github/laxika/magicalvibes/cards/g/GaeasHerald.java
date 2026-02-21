package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;

@CardRegistration(set = "10E", collectorNumber = "265")
public class GaeasHerald extends Card {

    public GaeasHerald() {
        addEffect(EffectSlot.STATIC, new CreatureSpellsCantBeCounteredEffect());
    }
}
