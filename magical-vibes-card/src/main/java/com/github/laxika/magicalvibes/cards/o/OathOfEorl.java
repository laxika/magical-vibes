package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "64")
public class OathOfEorl extends Card {

    public OathOfEorl() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                2, "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                2, "Human Knight", 2, 2, CardColor.RED,
                List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new PutCounterOnTargetPermanentEffect(CounterType.INDESTRUCTIBLE));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new BecomeMonarchEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                        "Target must be a Human"), 0, 1)));
    }
}
