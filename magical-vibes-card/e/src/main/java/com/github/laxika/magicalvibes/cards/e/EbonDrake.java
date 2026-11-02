package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "48")
public class EbonDrake extends Card {

    public EbonDrake() {
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                null, List.of(new LoseLifeEffect(1))));
    }
}
