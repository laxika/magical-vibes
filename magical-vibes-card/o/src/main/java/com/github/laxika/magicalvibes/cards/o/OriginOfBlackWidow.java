package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "664")
public class OriginOfBlackWidow extends Card {

    public OriginOfBlackWidow() {
        addTrigger(Triggers.chapter(1),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.eachOpponent()));
        addTrigger(Triggers.chapter(2),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.creaturesYouControl()));
        addTrigger(Triggers.chapter(3),
                LoseLifeEffect.eachTheirOwn(
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                        Subjects.eachOpponent()));
    }
}
