package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "19")
public class GleamOfAuthority extends Card {

    public GleamOfAuthority() {
        target(TargetFilters.creature());

        PermanentCounterSum countersOnOtherCreaturesYouControl = new PermanentCounterSum(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate())
                )),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                countersOnOtherCreaturesYouControl,
                countersOnOtherCreaturesYouControl,
                GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, "{W}", List.of(new BolsterEffect(1)),
                        "{W}, {T}: Bolster 1."),
                GrantScope.ENCHANTED_CREATURE));
    }
}
