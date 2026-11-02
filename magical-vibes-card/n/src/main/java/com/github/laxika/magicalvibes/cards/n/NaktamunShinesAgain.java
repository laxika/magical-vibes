package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetualBoostOwnCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "2")
public class NaktamunShinesAgain extends Card {

    public NaktamunShinesAgain() {
        addTrigger(Triggers.chapter(1),
                new PerpetualBoostOwnCreatureCardsEffect(2, 1));
        addTrigger(Triggers.chapter(2),
                new SeekLibraryEffect(new CardTypePredicate(CardType.CREATURE), 2));
        addTrigger(Triggers.chapter(3),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentMaxManaValuePredicate(2)))));
    }
}
