package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "260")
public class Delirium extends Card {

    public Delirium() {
        // "Cast this spell only during an opponent's turn."
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_TURN);

        // "Tap target creature that player controls. That creature deals damage equal to its power to
        // the player. Prevent all combat damage that would be dealt to and dealt by the creature this
        // turn." "That player" is the active player (an opponent, per the cast restriction), who is
        // also the target creature's controller.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByActivePlayerPredicate())),
                "Target must be a creature the active player controls"))
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToControllerEffect())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatToTargetCreatures())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());
    }
}
