package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "156")
public class MinaAndDennWildborn extends Card {

    public MinaAndDennWildborn() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{G}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{R}{G}, Return a land you control to its owner's hand: Target creature gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
