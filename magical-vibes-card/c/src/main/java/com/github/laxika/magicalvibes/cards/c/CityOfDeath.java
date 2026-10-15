package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "99")
@CardRegistration(set = "WHO", collectorNumber = "704")
public class CityOfDeath extends Card {

    private static final PermanentPredicate TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentIsTokenPredicate(),
            new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SAGA))));

    public CityOfDeath() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, CreateTokenEffect.ofTreasureToken(1));

        addCopyChapter(EffectSlot.SAGA_CHAPTER_II);
        addCopyChapter(EffectSlot.SAGA_CHAPTER_III);
        addCopyChapter(EffectSlot.SAGA_CHAPTER_IV);
        addCopyChapter(EffectSlot.SAGA_CHAPTER_V);
        addCopyChapter(EffectSlot.SAGA_CHAPTER_VI);
    }

    private void addCopyChapter(EffectSlot slot) {
        var filter = new ControlledPermanentPredicateTargetFilter(
                TARGET, "Target must be a non-Saga token you control.");
        addEffect(slot, new CreateTokenCopyOfTargetPermanentEffect());
        setSagaChapterTargetFilter(slot, Set.of(filter));
        setSagaChapterTargetGroups(slot, List.of(new SagaChapterTargetGroup(filter, 1, 1)));
    }
}
