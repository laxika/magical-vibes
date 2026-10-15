package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;

@CardRegistration(set = "YMKM", collectorNumber = "12")
public class SnarlfangVermin extends Card {

    public SnarlfangVermin() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect());
        addEffect(EffectSlot.GRAVEYARD_ON_OPPONENT_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSuspectedPredicate(),
                        new ConditionalEffect(new SourceCardInGraveyard(),
                                new LoseLifeEffect(1, LoseLifeRecipient.DYING_CREATURE_CONTROLLER))));
    }
}
