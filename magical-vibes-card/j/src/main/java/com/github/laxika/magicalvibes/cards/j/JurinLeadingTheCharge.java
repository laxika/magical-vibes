package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1240")
@CardRegistration(set = "SLX", collectorNumber = "27")
public class JurinLeadingTheCharge extends Card {

    public JurinLeadingTheCharge() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());

        addTrigger(Triggers.attacks(), new BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect());
    }
}
