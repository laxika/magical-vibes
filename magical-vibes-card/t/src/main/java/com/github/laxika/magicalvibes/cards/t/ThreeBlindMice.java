package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "35")
public class ThreeBlindMice extends Card {

    public ThreeBlindMice() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Mouse", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.MOUSE), Set.of(), Set.of()));

        addTrigger(Triggers.chapter(2), new CreateTokenCopyOfTargetPermanentEffect());
        setSagaChapterTargetFilter(2, Set.of(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsTokenPredicate(), "Target must be a token you control")));
        setSagaChapterTargetRequired(2);

        addTrigger(Triggers.chapter(3), new CreateTokenCopyOfTargetPermanentEffect());
        setSagaChapterTargetFilter(3, Set.of(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsTokenPredicate(), "Target must be a token you control")));
        setSagaChapterTargetRequired(3);

        addTrigger(Triggers.chapter(4),
                new BoostAllOwnCreaturesEffect(1, 1),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES));
    }
}
