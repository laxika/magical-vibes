package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "41")
@CardRegistration(set = "FIC", collectorNumber = "145")
public class RikkuResourcefulGuardian extends Card {

    public RikkuResourcefulGuardian() {
        addEffect(EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE, new MakeCreatureUnblockableEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MoveCounterFromTargetCreatureToTargetCreatureEffect()),
                "{1}, {T}: Move a counter from target creature an opponent controls onto target creature you control. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED,
                List.of(TargetFilters.creatureAnOpponentControls(), TargetFilters.creatureYouControl()),
                2,
                2
        ));
    }
}
