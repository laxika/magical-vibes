package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "60")
@CardRegistration(set = "FIC", collectorNumber = "198")
public class SummonEsperValigarmanda extends Card {

    public SummonEsperValigarmanda() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(instantOrSorcery));

        for (EffectSlot chapter : Set.of(
                EffectSlot.SAGA_CHAPTER_II,
                EffectSlot.SAGA_CHAPTER_III,
                EffectSlot.SAGA_CHAPTER_IV)) {
            addEffect(chapter,
                    new AwardManaEffect(ManaColor.RED, new CountersOnSource(CounterType.LORE)));
            addEffect(chapter,
                    new MayCastInstantOrSorceryCardsExiledWithSourceEffect(false, true, false));
        }
    }
}
