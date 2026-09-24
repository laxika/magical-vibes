package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "3")
@CardRegistration(set = "TMC", collectorNumber = "89")
public class SplinterTheMentor extends Card {

    public SplinterTheMentor() {
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, mutagenToken());
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()), mutagenToken()));
    }

    private static CreateTokenEffect mutagenToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Mutagen", List.of(), List.of(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(
                                new SacrificeSelfCost(),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
                        ),
                        "{1}, {T}, Sacrifice this token: Put a +1/+1 counter on target creature. "
                                + "Activate only as a sorcery.",
                        TargetFilters.creature(),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                )));
    }
}
