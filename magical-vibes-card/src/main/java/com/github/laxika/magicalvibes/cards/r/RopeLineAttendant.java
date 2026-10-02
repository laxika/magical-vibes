package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSNC", collectorNumber = "27")
public class RopeLineAttendant extends Card {

    public RopeLineAttendant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        List.of(new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN))),
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
