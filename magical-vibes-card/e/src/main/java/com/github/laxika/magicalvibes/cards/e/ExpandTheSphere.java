package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ONE", collectorNumber = "168")
public class ExpandTheSphere extends Card {

    public ExpandTheSphere() {
        addSpellEffect(LookAtTopCardsEffect.mayPutUpToMatchingOntoBattlefieldTappedRestOnBottomRandom(
                        6, new CardTypePredicate(CardType.LAND), 2, true));
        addSpellEffect(new ProliferateEffect(
                new Sum(new Fixed(2), new Scaled(new EventValue(), -1))));
    }
}
