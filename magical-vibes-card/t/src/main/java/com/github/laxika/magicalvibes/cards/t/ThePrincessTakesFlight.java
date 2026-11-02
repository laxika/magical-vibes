package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "23")
public class ThePrincessTakesFlight extends Card {

    public ThePrincessTakesFlight() {
        addTrigger(Triggers.chapter(1), new ExileTargetPermanentAndTrackWithSourceEffect());
        setSagaChapterTargetFilter(1, Set.of(TargetFilters.creature()));

        addTrigger(Triggers.chapter(2), new BoostTargetCreatureEffect(2, 2));
        addTrigger(Triggers.chapter(2), new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        setSagaChapterTargetGroups(2, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        addTrigger(Triggers.chapter(3), new ReturnAllCardsExiledWithSourceEffect());
    }
}
