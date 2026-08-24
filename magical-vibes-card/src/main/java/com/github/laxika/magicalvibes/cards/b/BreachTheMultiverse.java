package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "94")
public class BreachTheMultiverse extends Card {

    public BreachTheMultiverse() {
        addEffect(EffectSlot.SPELL, new MillEffect(10, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new MillEffect(10, MillRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCardFromGraveyardToBattlefieldEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.PLANESWALKER)
                ))));
        addEffect(EffectSlot.SPELL, new GrantSubtypeToOwnCreaturesEffect(CardSubtype.PHYREXIAN));
    }
}
