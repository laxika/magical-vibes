package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AA2", collectorNumber = "19")
@CardRegistration(set = "NCC", collectorNumber = "81")
@CardRegistration(set = "NCC", collectorNumber = "181")
@CardRegistration(set = "SOC", collectorNumber = "345")
@CardRegistration(set = "MSC", collectorNumber = "197")
@CardRegistration(set = "MSC", collectorNumber = "430")
public class CurrencyConverter extends Card {

    public CurrencyConverter() {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addTrigger(Triggers.youDiscardCard(), new MayEffect(
                new ExileDiscardedCardFromGraveyardEffect(true),
                "Exile that card from your graveyard?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, Subjects.you())),
                "{2}, {T}: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        ChooseCardEffect.oneOf(Subjects.exiledWithSource(),
                                "a card exiled with this artifact to put into its owner's graveyard"),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.CHOOSE, land),
                                CreateTokenEffect.ofTreasureToken(1)),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.CHOOSE, new CardNotPredicate(land)),
                                new CreateTokenEffect("Rogue", 2, 2, CardColor.BLACK,
                                        List.of(CardSubtype.ROGUE), Set.of(), Set.of()))),
                "{T}: Put a card exiled with this artifact into its owner's graveyard. If it's a land card, create a Treasure token. If it's a nonland card, create a 2/2 black Rogue creature token."
        ));
    }
}
