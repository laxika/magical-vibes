package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "253")
public class BretagardStronghold extends Card {

    public BretagardStronghold() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {G}{W}{W}, {T}, Sacrifice this land: Put a +1/+1 counter on each of up to two target
        // creatures you control. They gain vigilance and lifelink until end of turn. Activate only
        // as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{W}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.LIFELINK), GrantScope.TARGET)
                ),
                "{G}{W}{W}, {T}, Sacrifice this land: Put a +1/+1 counter on each of up to two target "
                        + "creatures you control. They gain vigilance and lifelink until end of turn. "
                        + "Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureYouControl()),
                0,
                2
        ));
    }
}
