package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "89")
public class GrimJavelineer extends Card {

    public GrimJavelineer() {
        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.youAttack(), new BoostTargetCreatureEffect(1, 0))
                .addTrigger(Triggers.youAttack(),
                        new ResolveEffectOnTargetDeathThisTurnEffect(new SurveilEffect(1)));
    }
}
