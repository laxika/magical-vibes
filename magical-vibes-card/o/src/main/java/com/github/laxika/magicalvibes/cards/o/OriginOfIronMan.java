package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "633")
public class OriginOfIronMan extends Card {

    public OriginOfIronMan() {
        // Chapter I: Tap up to one target creature. It doesn't untap during its controller's
        // untap step for as long as this Saga remains on the battlefield.
        addEffect(EffectSlot.SAGA_CHAPTER_I, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_I, DoesntUntapEffect.targetWhileSourceOnBattlefield());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        // Chapter II: Draw two cards.
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DrawCardEffect(2));

        // Chapter III: You may put an artifact card with mana value 5 or less from your hand
        // onto the battlefield.
        CardAllOfPredicate artifactWithManaValueAtMostFive = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardMaxManaValuePredicate(5)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new MayEffect(
                new PutCardToBattlefieldEffect(artifactWithManaValueAtMostFive, "artifact"),
                "Put an artifact card with mana value 5 or less from your hand onto the battlefield?"));
    }
}
