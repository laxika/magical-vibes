package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.HostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHostedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "327")
@CardRegistration(set = "MB2", collectorNumber = "564")
public class ImmersturmBattlefield extends Card {

    public ImmersturmBattlefield() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                2, 0, Set.of(Keyword.HASTE), GrantScope.ALL_CREATURES,
                new PermanentHostedBySourcePredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new HostTargetCreatureEffect()),
                "Host target creature at this Realm. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
