package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOrBlockingAlonePredicate;

@CardRegistration(set = "WHO", collectorNumber = "111")
@CardRegistration(set = "WHO", collectorNumber = "716")
public class ThijarianWitness extends Card {

    public ThijarianWitness() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsAttackingOrBlockingAlonePredicate(),
                        SequenceEffect.of(
                                new ExileTriggeringCreatureFromGraveyardEffect(),
                                CreateTokenEffect.ofClueToken(1))));
    }
}
