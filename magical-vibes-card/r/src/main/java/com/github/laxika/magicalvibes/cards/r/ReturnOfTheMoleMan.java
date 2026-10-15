package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "729")
public class ReturnOfTheMoleMan extends Card {

    public ReturnOfTheMoleMan() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));

        CardsInGraveyard permanentCardsInGraveyard = new CardsInGraveyard(
                new CardIsPermanentPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(new SacrificeSelfCost(), moloidTokens(permanentCardsInGraveyard)),
                "{5}{G}, Sacrifice Return of the Mole Man: Create X 1/1 green Minion creature tokens named Moloid, where X is the number of permanent cards in your graveyard. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static CreateTokenEffect moloidTokens(CardsInGraveyard amount) {
        return new CreateTokenEffect(
                CardType.CREATURE,
                amount,
                "Moloid",
                1,
                1,
                CardColor.GREEN,
                Set.of(CardColor.GREEN),
                List.of(CardSubtype.MINION),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.ON_ATTACK,
                        new MayEffect(new MillEffect(1, MillRecipient.CONTROLLER), "Mill a card?")),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
