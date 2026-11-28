package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "NPH", collectorNumber = "115")
public class MeliraSylvokOutcast extends Card {

    public MeliraSylvokOutcast() {
        // You can't get poison counters.
        addStaticEffect(new PlayerCantGetPoisonCountersEffect());

        // Creatures you control can't have -1/-1 counters put on them.
        addStaticEffect(new GrantNonAbilityEffectEffect(
                CantHaveCountersEffect.ofKind(CounterType.MINUS_ONE_MINUS_ONE), Subjects.creaturesYouControl()));

        // Creatures your opponents control lose infect.
        addStaticEffect(new RemoveKeywordEffect(Keyword.INFECT, Subjects.creaturesYourOpponentsControl()));
    }
}
