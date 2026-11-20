package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "40K", collectorNumber = "71")
public class AspiringChampion extends Card {

    public AspiringChampion() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect())));
    }
}
