package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "57")
@CardRegistration(set = "M3C", collectorNumber = "109")
public class HideousTaskmaster extends Card {

    public HideousTaskmaster() {
        // Annihilator 1 (printed).
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addTrigger(Triggers.youCastThisSpell(),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addTrigger(Triggers.youCastThisSpell(),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addTrigger(Triggers.youCastThisSpell(),
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.TARGET))
                .addTrigger(Triggers.youCastThisSpell(),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_ATTACK,
                                new SacrificePermanentsEffect(
                                        1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER)));
    }
}
