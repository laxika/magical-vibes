package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "170")
public class HellcatUndyingVigilante extends Card {

    public HellcatUndyingVigilante() {
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsSelfPredicate())
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .returnAll(true)
                .underOwnersControl(true)
                .plusOneCounterCount(1)
                .battlefieldEffectGrants(List.of(
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET, GrantDuration.INDEFINITE)))
                .build());
    }
}
