package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "64")
public class FireNationTurret extends Card {

    public FireNationTurret() {
        // At the beginning of combat on your turn, up to one target creature gets +2/+0 and
        // gains firebending 2 until end of turn.
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), BoostEffect.target(2, 0),
                        new GrantKeywordEffect(Keyword.FIREBENDING, Subjects.target()),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.attacks(),
                                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 2))));

        // {R}: Put a charge counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(PutCountersEffect.onSelf(CounterType.CHARGE)),
                "{R}: Put a charge counter on this artifact."
        ));

        // Remove fifty charge counters from this artifact: It deals 50 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(50, CounterType.CHARGE),
                        DealDamageEffect.toAnyTarget(50)
                ),
                "Remove fifty charge counters from this artifact: It deals 50 damage to any target."
        ));
    }
}
