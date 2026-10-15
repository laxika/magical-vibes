package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "179")
@CardRegistration(set = "TMT", collectorNumber = "222")
public class Technodrome extends Card {

    public Technodrome() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new SourcePowerAtLeast(6),
                "its power is 6 or greater"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "another artifact"),
                        new DrawCardEffect(1),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                "{T}, Sacrifice another artifact: Draw a card. Put a +1/+1 counter on this creature."
        ));
    }
}
