package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "62")
public class DragonsoulKnight extends Card {

    public DragonsoulKnight() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{U}{B}{R}{G}", List.of(
                new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.DRAGON),
                new BoostSelfEffect(5, 3),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{W}{U}{B}{R}{G}: Until end of turn, this creature becomes a Dragon, gets +5/+3, and gains flying and trample."));
    }
}
