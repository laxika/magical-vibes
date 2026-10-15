package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "11")
@CardRegistration(set = "MOC", collectorNumber = "98")
public class ChivalricAlliance extends Card {

    public ChivalricAlliance() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumAttackers(2), new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                1, "Knight", 2, 2, null,
                                Set.of(CardColor.WHITE, CardColor.BLUE),
                                List.of(CardSubtype.KNIGHT),
                                Set.of(Keyword.VIGILANCE), Set.of())),
                "{2}, Discard a card: Create a 2/2 white and blue Knight creature token with vigilance."));
    }
}
