package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "170")
public class SeizeTheStorm extends Card {

    public SeizeTheStorm() {
        // Create a red Elemental with trample whose P/T equal:
        // instant/sorcery cards in your GY + cards with flashback you own in exile.
        DynamicAmount powerToughness = new Sum(
                new CardsInGraveyard(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )), CountScope.CONTROLLER),
                new CardsInExile(new CardHasFlashbackPredicate(), CountScope.CONTROLLER)
        );
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Elemental", 0, 0,
                CardColor.RED, List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE), Set.of(),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(powerToughness, powerToughness))
        ));
        addCastingOption(new FlashbackCast("{6}{R}"));
    }
}
