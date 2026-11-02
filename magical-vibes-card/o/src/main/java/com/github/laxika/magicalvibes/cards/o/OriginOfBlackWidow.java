package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "664")
public class OriginOfBlackWidow extends Card {

    public OriginOfBlackWidow() {
        addTrigger(Triggers.chapter(1),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(2),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.chapter(3),
                new EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
