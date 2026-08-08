package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Showstopper — Until end of turn, creatures you control gain "When this creature dies, it deals
 * 2 damage to target creature an opponent controls."
 *
 * <p>The grant is a snapshot of the creatures controlled as it resolves. The granted death trigger
 * carries its own "creature an opponent controls" narrowing on the damage effect, because the death
 * pipeline reads the restriction off the dying creature's card — which is not the card that granted
 * the ability.
 */
@CardRegistration(set = "DGM", collectorNumber = "102")
public class Showstopper extends Card {

    public Showstopper() {
        addEffect(EffectSlot.SPELL, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_DEATH,
                new DealDamageToTargetCreatureEffect(2, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))))));
    }
}
