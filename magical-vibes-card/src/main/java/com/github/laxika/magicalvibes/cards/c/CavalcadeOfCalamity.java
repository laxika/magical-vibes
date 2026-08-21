package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "RNA", collectorNumber = "95")
public class CavalcadeOfCalamity extends Card {

    public CavalcadeOfCalamity() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentPowerAtMostPredicate(1),
                        new DealDamageToAttackedTargetEffect(1)));
    }
}
