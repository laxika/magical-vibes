package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "158")
@CardRegistration(set = "MMQ", collectorNumber = "146")
public class MidnightRitual extends Card {

    public MidnightRitual() {
        // Exile X target creature cards from your graveyard. For each creature card exiled this way,
        // create a 2/2 black Zombie creature token.
        addSpellEffect(ExileCardsFromGraveyardEffect.exactlyXFromOwnGraveyard(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new CreateTokenEffect(new AmountThisWay(StepKind.EXILE), "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));
    }
}
