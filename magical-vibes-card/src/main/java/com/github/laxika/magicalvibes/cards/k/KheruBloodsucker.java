package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "75")
public class KheruBloodsucker extends Card {

    public KheruBloodsucker() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentToughnessAtLeastPredicate(4),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(2))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                "{2}{B}, Sacrifice another creature: Put a +1/+1 counter on Kheru Bloodsucker."
        ));
    }
}
