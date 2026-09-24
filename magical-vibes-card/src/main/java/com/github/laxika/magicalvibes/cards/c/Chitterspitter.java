package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "153")
public class Chitterspitter extends Card {

    public Chitterspitter() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsTokenPredicate(),
                        new PutCountersOnSelfEffect(CounterType.ACORN),
                        "a token"),
                "Sacrifice a token?"));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL),
                CounterType.ACORN, false));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new CreateTokenEffect(
                        "Squirrel", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())),
                "{G}, {T}: Create a 1/1 green Squirrel creature token."));
    }
}
