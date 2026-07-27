package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "137")
public class CaptivatingCrew extends Card {

    public CaptivatingCrew() {
        // {3}{R}: Gain control of target creature an opponent controls until end of turn.
        // Untap that creature. It gains haste until end of turn. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}{R}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{3}{R}: Gain control of target creature an opponent controls until end of turn. Untap that creature. It gains haste until end of turn. Activate only as a sorcery.",
                TargetFilters.creatureAnOpponentControls(),
                null, null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
