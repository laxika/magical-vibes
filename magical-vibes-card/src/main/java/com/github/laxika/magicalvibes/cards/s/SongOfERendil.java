package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "69")
public class SongOfERendil extends Card {

    public SongOfERendil() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ScryEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardEffect(2));

        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Bird", 2, 2, CardColor.BLUE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new PutCounterOnEachControlledPermanentEffect(
                CounterType.FLYING, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                ))));
    }
}
