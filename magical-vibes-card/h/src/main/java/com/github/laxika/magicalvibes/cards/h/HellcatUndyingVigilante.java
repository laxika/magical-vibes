package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "170")
public class HellcatUndyingVigilante extends Card {

    public HellcatUndyingVigilante() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsSelfPredicate())
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .returnAll(true)
                .underOwnersControl(true)
                .plusOneCounterCount(1)
                .battlefieldEffectGrants(List.of(
                        new LosesAllAbilitiesEffect(Subjects.target(), Duration.PERMANENT),
                        new GrantKeywordEffect(Keyword.HASTE, Subjects.target(), Duration.PERMANENT)))
                .build());
    }
}
