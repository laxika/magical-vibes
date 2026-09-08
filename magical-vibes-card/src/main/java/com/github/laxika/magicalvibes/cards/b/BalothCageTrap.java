package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "156")
public class BalothCageTrap extends Card {

    public BalothCageTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{G}")),
                new OpponentPermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 1),
                false));

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Beast",
                4,
                4,
                CardColor.GREEN,
                List.of(CardSubtype.BEAST),
                Set.of(),
                Set.of()));
    }
}
