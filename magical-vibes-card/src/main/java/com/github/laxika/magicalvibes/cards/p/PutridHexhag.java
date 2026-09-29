package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YECL", collectorNumber = "10")
public class PutridHexhag extends Card {

    public PutridHexhag() {
        PermanentPredicate opponentCreature = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_SELF_COUNTERS_PUT,
                        new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
                                EffectSlot.ON_DEATH,
                                new LoseLifeEffect(2),
                                opponentCreature));
    }
}
