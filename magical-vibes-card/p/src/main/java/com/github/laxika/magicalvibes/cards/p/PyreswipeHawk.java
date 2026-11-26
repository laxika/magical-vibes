package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "26")
@CardRegistration(set = "BLC", collectorNumber = "60")
public class PyreswipeHawk extends Card {

    public PyreswipeHawk() {
        addTrigger(Triggers.attacks(), BoostEffect.self(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                new Fixed(0)));

        target(TargetFilters.artifact(), 0, 1).addTrigger(Triggers.youCastSpell().expend(6),
                new GainControlOfTargetEffect(Duration.WHILE_SOURCE_ON_BATTLEFIELD));
    }
}
