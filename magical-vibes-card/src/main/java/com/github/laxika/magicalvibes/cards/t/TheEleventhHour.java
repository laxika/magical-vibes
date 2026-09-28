package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "41")
public class TheEleventhHour extends Card {

    public TheEleventhHour() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new SearchLibraryEffect(
                new CardSubtypePredicate(CardSubtype.DOCTOR), LibrarySearchDestination.HAND));

        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofFoodToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                        new CardTypePredicate(CardType.CREATURE), 1, CostModificationScope.SELF))));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenCopyOfTargetPermanentEffect(
                "Prisoner Zero", List.of(CardSubtype.ALIEN), Set.of(CardSupertype.LEGENDARY)));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
