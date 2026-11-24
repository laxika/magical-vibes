package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "214")
public class MimeoplasmReveredOne extends Card {

    public MimeoplasmReveredOne() {
        addStaticEffect(new ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect(3));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(BecomeCopyOfSubjectEffect.of(Subjects.targetCardExiledWithSource(
                                new CardTypePredicate(CardType.CREATURE)))
                        .keepingThisAbility()
                        .except(CopyExceptions.withBasePowerToughness(0, 0))),
                "Become a copy of target creature card exiled with this creature, except it's 0/0 and has this ability."));
    }
}
