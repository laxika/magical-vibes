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

import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "57")
@CardRegistration(set = "M3C", collectorNumber = "109")
public class HideousTaskmaster extends Card {

    public HideousTaskmaster() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_ATTACK,
                                new SacrificePermanentsEffect(
                                        1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER)));
    }
}
