package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1184")
public class LordWindgrace extends Card {

    public LordWindgrace() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DiscardCardThenEffect(
                        null,
                        new DrawCardEffect(),
                        "a card",
                        CardType.LAND,
                        new DrawCardEffect(2))),
                "+2: Discard a card, then draw a card. If a land card is discarded this way, draw an additional card."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.LAND),
                        2,
                        false,
                        false)),
                "−3: Return up to two target land cards from your graveyard to the battlefield.",
                new GraveyardCardPredicateTargetFilter(
                        new CardTypePredicate(CardType.LAND),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                -3,
                null,
                null,
                List.of(),
                0,
                2
        ));

        PermanentNotPredicate nonlandPermanent = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DestroyEachTargetPermanentEffect(),
                        new CreateTokenEffect(
                                6,
                                "Cat Warrior",
                                2,
                                2,
                                CardColor.GREEN,
                                List.of(CardSubtype.CAT, CardSubtype.WARRIOR),
                                Set.of(Keyword.FORESTWALK),
                                Set.of())),
                "−11: Destroy up to six target nonland permanents, then create six 2/2 green Cat Warrior creature tokens with forestwalk.",
                new PermanentPredicateTargetFilter(nonlandPermanent,
                        "Targets must be nonland permanents"),
                -11,
                null,
                null,
                List.of(),
                0,
                6
        ));
    }
}
