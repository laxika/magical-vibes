package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "120")
@CardRegistration(set = "TMT", collectorNumber = "198")
public class MichelangeloMutantBFF extends Card {

    public MichelangeloMutantBFF() {
        addEffect(EffectSlot.STATIC, new EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(
                1, new PermanentHasCountersPredicate(CounterType.ANY)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, mutagenToken());
        addEffect(EffectSlot.ON_ATTACK, mutagenToken());
    }

    private static CreateTokenEffect mutagenToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Mutagen",
                List.of(),
                List.of(new ActivatedAbility(
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
