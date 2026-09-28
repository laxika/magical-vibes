package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "MB2", collectorNumber = "322")
@CardRegistration(set = "MB2", collectorNumber = "558")
public class BolshackDragon extends Card {

    public BolshackDragon() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));

        CardsInGraveyard redCards = new CardsInGraveyard(
                new CardColorPredicate(CardColor.RED), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsAttacking(),
                new BoostSelfEffect(redCards, new Fixed(0))));
    }
}
