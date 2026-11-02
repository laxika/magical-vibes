package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostControllerNoncombatDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "234")
public class TaiiWakeenPerfectShot extends Card {

    public TaiiWakeenPerfectShot() {
        addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToCreature(), new DrawCardEffect(1));
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new BoostControllerNoncombatDamageThisTurnEffect()),
                "{X}, {T}: If a source you control would deal noncombat damage to a permanent or player this turn, it deals that much damage plus X instead."));
    }
}
