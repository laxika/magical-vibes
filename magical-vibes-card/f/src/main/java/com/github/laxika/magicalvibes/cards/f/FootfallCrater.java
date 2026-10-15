package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "118")
public class FootfallCrater extends Card {

    public FootfallCrater() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new GrantKeywordEffect(
                                        Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                                        GrantScope.TARGET,
                                        new PermanentIsCreaturePredicate()
                                )),
                                "{T}: Target creature gains trample and haste until end of turn.",
                                TargetFilters.creature()
                        ),
                        GrantScope.ENCHANTED_PERMANENT
                ));
        addCycling("{1}");
    }
}
