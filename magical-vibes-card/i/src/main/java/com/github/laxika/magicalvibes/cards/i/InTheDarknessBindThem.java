package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "58")
@CardRegistration(set = "LTC", collectorNumber = "465")
public class InTheDarknessBindThem extends Card {

    public InTheDarknessBindThem() {
        var wraith = new CreateTokenEffect(1, "Wraith", 3, 3, CardColor.BLACK,
                List.of(CardSubtype.WRAITH), Set.of(Keyword.MENACE), Set.of());
        addTrigger(Triggers.chapter(1), wraith, new RingTemptsYouEffect());
        addTrigger(Triggers.chapter(2), wraith, new RingTemptsYouEffect());
        addTrigger(Triggers.chapter(3), wraith, new RingTemptsYouEffect());

        var target = TargetFilters.creatureAnOpponentControls();
        target(target, 0, 99)
                .addTrigger(Triggers.chapter(4),
                        GainControlOfTargetEffect.withTargetPredicate(
                                ControlDuration.END_OF_TURN, target.predicate()),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new RingTemptsYouEffect());
        setSagaChapterTargetFilter(4, Set.of(target));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
    }
}
