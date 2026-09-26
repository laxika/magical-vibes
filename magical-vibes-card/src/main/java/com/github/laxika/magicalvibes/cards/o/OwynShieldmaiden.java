package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "1")
@CardRegistration(set = "LTC", collectorNumber = "81")
@CardRegistration(set = "LTC", collectorNumber = "86")
public class OwynShieldmaiden extends Card {

    public OwynShieldmaiden() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new AnotherPermanentEnteredThisTurn(new CardSubtypePredicate(CardSubtype.HUMAN)),
                SequenceEffect.of(
                        new CreateTokenEffect(
                                2, "Human Knight", 2, 2, CardColor.RED,
                                List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT),
                                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()),
                        new ConditionalEffect(
                                new ControlsPermanentCount(6,
                                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                                new DrawCardEffect()))));
    }
}
