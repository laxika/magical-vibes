package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "78")
public class BasilicaStalker extends Card {

    public BasilicaStalker() {
        addMorph("{4}{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(new GainLifeEffect(1), new SurveilEffect(1)));
    }
}
