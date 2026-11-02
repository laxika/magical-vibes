package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "120")
public class GogglesOfNight extends Card {

    public GogglesOfNight() {
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ScryEffect(1),
                        new DrawCardEffect(1)
                ));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
