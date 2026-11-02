package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "75")
public class KheruBloodsucker extends Card {

    private static final CardEffect DEATH_TRIGGER =
            SequenceEffect.of(
                            new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                            new GainLifeEffect(2));

    public KheruBloodsucker() {
        addTrigger(Triggers.creatureDies(new PermanentToughnessAtLeastPredicate(4), ObjectRelation.YOU_CONTROL),
                DEATH_TRIGGER);

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
