package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MKM", collectorNumber = "78")
public class BasilicaStalker extends Card {

    public BasilicaStalker() {
        addMorph("{4}{B}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SurveilEffect(1));
    }
}
