package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "47")
public class SewerPlague extends Card {

    public SewerPlague() {
        PermanentPredicate opponentCreature = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new PerpetuallyBoostTargetCreatureEffect(-2, -2,
                        opponentCreature))
                .addEffect(EffectSlot.SPELL, new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
                        EffectSlot.EACH_UPKEEP_TRIGGERED,
                        new PerpetuallyBoostSourceEffect(-1, -1),
                        opponentCreature));
    }
}
