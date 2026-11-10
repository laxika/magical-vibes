package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "60")
@CardRegistration(set = "FIC", collectorNumber = "198")
public class SummonEsperValigarmanda extends Card {

    public SummonEsperValigarmanda() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addTrigger(Triggers.chapter(1),
                new ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(instantOrSorcery, true));

        for (int chapter : Set.of(2, 3, 4)) {
            addTrigger(Triggers.chapter(chapter),
                    new AwardManaEffect(ManaColor.RED, new CountersOnSource(CounterType.LORE)),
                    new MayCastInstantOrSorceryCardsExiledWithSourceEffect(false, true, false));
        }
    }
}
