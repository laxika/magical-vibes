package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KirinTouchedOrochi;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "212")
public class TeachingsOfTheKirin extends Card {

    public TeachingsOfTheKirin() {
        setBackFaceCard(new KirinTouchedOrochi());

        addTrigger(Triggers.chapter(1), new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        setSagaChapterTargetGroups(2,
                List.of(new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "KirinTouchedOrochi";
    }
}
