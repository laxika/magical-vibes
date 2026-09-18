package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "20")
@CardRegistration(set = "ACR", collectorNumber = "118")
public class LeonardoDaVinci extends Card {

    public LeonardoDaVinci() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(new SetAllOwnCreaturesBasePowerToughnessEffect(
                        cardsInHand,
                        cardsInHand,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.THOPTER))))),
                "{3}{U}{U}: Until end of turn, Thopters you control have base power and toughness X/X, "
                        + "where X is the number of cards in your hand."));

        CreateTokenCopyOfTargetPermanentEffect thopterCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.THOPTER),
                Set.of(CardType.ARTIFACT, CardType.CREATURE),
                0,
                2,
                Map.of(),
                null,
                Set.of(Keyword.FLYING));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new DrawCardEffect(),
                        new DiscardCardThenEffect(
                                null,
                                new ExileDiscardedCardAndCreateTokenCopyEffect(thopterCopy),
                                "a card",
                                new CardTypePredicate(CardType.ARTIFACT))),
                "{2}{U}, {T}: Draw a card, then discard a card. If the discarded card was an artifact card, "
                        + "exile it from your graveyard. If you do, create a token that's a copy of it, except "
                        + "it's a 0/2 Thopter artifact creature with flying in addition to its other types."));
    }
}
