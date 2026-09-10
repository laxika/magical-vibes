package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNameStartsWithPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
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

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new MillControllerTwoRepeatIfSharedCardTypeThenDrawEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new ExileGraveyardCardsAndMayCastCopiesEffect(
                        instantSorceryOrTamiyoPlaneswalker, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
