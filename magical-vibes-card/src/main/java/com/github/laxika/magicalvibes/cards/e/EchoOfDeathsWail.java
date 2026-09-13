package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

public class EchoOfDeathsWail extends Card {

    private static final PermanentAllOfPredicate RAT_TOKEN = new PermanentAllOfPredicate(List.of(
            new PermanentIsTokenPredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.RAT)));

    private static final PermanentAllOfPredicate ANOTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public EchoOfDeathsWail() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainControlOfAllPermanentsMatchingEffect(RAT_TOKEN));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(ANOTHER_CREATURE, new DrawCardEffect(1),
                        "another creature"),
                "Sacrifice another creature?"));
    }
}
