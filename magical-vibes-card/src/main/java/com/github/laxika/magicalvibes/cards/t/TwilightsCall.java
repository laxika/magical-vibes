package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "130")
public class TwilightsCall extends Card {

    public TwilightsCall() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{6}{B}{B}")), null, true));
        addEffect(EffectSlot.SPELL, new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                Integer.MAX_VALUE, new CardTypePredicate(CardType.CREATURE)));
    }
}
