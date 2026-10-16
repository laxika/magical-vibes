package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

@CardRegistration(set = "SLD", collectorNumber = "1240")
@CardRegistration(set = "SLX", collectorNumber = "27")
public class JurinLeadingTheCharge extends Card {

    public JurinLeadingTheCharge() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());

        addEffect(EffectSlot.ON_ATTACK, new BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect());
    }
}
