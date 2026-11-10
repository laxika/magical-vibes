package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "633")
public class OriginOfIronMan extends Card {

    public OriginOfIronMan() {
        // Chapter I: Tap up to one target creature. It doesn't untap during its controller's
        // untap step for as long as this Saga remains on the battlefield.
        addTrigger(Triggers.chapter(1),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                DoesntUntapEffect.targetWhileSourceOnBattlefield());
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 1)));

        // Chapter II: Draw two cards.
        addTrigger(Triggers.chapter(2), new DrawCardEffect(2));

        // Chapter III: You may put an artifact card with mana value 5 or less from your hand
        // onto the battlefield.
        CardAllOfPredicate artifactWithManaValueAtMostFive = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardMaxManaValuePredicate(5)));
        addTrigger(Triggers.chapter(3), new MayEffect(
                new PutCardToBattlefieldEffect(artifactWithManaValueAtMostFive, "artifact"),
                "Put an artifact card with mana value 5 or less from your hand onto the battlefield?"));
    }
}
