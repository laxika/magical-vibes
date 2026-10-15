package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "73")
public class WishfulMerfolk extends Card {

    public WishfulMerfolk() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(
                new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF),
                new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.HUMAN)),
                "{1}{U}: This creature loses defender and becomes a Human until end of turn."));
    }
}
