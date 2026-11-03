package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BIG", collectorNumber = "20")
public class VaultbornTyrant extends Card {

    public VaultbornTyrant() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtLeastPredicate(4))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                SequenceEffect.of(new GainLifeEffect(3), new DrawCardEffect(1)));
        addTrigger(Triggers.dies(Triggers.NONTOKEN, ObjectRelation.THIS),
                new CreateTokenCopyOfSourceEffect(false, 1, Set.of(CardType.ARTIFACT)));
    }
}
