package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOrBlockingAlonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "111")
@CardRegistration(set = "WHO", collectorNumber = "716")
public class ThijarianWitness extends Card {

    public ThijarianWitness() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsAttackingOrBlockingAlonePredicate(),
                        SequenceEffect.of(
                                new ExileTriggeringCreatureFromGraveyardEffect(),
                                CreateTokenEffect.ofClueToken(1))));
    }
}
