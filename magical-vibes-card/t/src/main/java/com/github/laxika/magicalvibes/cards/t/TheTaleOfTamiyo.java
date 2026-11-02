package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNameStartsWithPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "75")
public class TheTaleOfTamiyo extends Card {

    public TheTaleOfTamiyo() {
        CardPredicate instantSorceryOrTamiyoPlaneswalker = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.PLANESWALKER),
                        new CardNameStartsWithPredicate("Tamiyo")))));

        addTrigger(Triggers.chapter(1),
                new MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect());
        addTrigger(Triggers.chapter(2),
                new MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect());
        addTrigger(Triggers.chapter(3),
                new MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect());
        addTrigger(Triggers.chapter(4),
                new ExileGraveyardCardsAndMayCastCopiesEffect(
                        instantSorceryOrTamiyoPlaneswalker, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
