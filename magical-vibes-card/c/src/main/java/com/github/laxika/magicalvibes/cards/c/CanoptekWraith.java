package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "40K", collectorNumber = "153")
public class CanoptekWraith extends Card {

    public CanoptekWraith() {
        addStaticEffect(new CantBeBlockedEffect());
        // "Pay {3} and sacrifice it" can't be paid once it has left the battlefield
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ConditionalEffect(new SourceIsOnBattlefield(),
                new MayPayManaEffect("{3}", SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect())),
                        "Pay {3} and sacrifice Canoptek Wraith?")));
    }
}
