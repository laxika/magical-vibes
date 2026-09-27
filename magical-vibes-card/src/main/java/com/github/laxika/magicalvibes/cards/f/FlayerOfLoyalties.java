package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "715")
@CardRegistration(set = "CMM", collectorNumber = "747")
public class FlayerOfLoyalties extends Card {

    public FlayerOfLoyalties() {
        // When you cast this spell, gain control of target creature until end of turn.
        // Untap it. Until end of turn, it has base power and toughness 10/10 and gains trample,
        // annihilator 2, and haste.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_SELF_CAST, new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.ON_SELF_CAST, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_CAST, new SetBasePowerToughnessEffect(10, 10))
                .addEffect(EffectSlot.ON_SELF_CAST,
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_CAST, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_ATTACK,
                        new SacrificePermanentsEffect(
                                2, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER)));
    }
}
