package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromAmongExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "86")
@CardRegistration(set = "KHM", collectorNumber = "351")
public class DraugrNecromancer extends Card {

    public DraugrNecromancer() {
        addStaticEffect(ExileOpponentCreaturesInsteadOfDyingEffect.withIceCounter());
        addStaticEffect(new PlayFromAmongExiledCardsEffect(Subjects.exiledCards(Subjects.eachOpponent(),
                new CardAllOfPredicate(List.of(new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardHasCounterInExilePredicate(CounterType.ICE)))), true));
    }
}
