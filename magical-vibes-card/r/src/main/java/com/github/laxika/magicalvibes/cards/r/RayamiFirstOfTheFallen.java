package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "48")
public class RayamiFirstOfTheFallen extends Card {

    public RayamiFirstOfTheFallen() {
        addStaticEffect(new ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect());
        addStaticEffect(new GainKeywordsOfCreatureCardsExiledWithSourceEffect(
                Subjects.exiledCards(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardHasCounterInExilePredicate(CounterType.BLOOD)))),
                GainKeywordsOfCreatureCardsExiledWithSourceEffect.COMMON_KEYWORDS, true));
    }
}
