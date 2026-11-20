package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "270")
@CardRegistration(set = "INR", collectorNumber = "446")
@CardRegistration(set = "EMN", collectorNumber = "199")
@CardRegistration(set = "SIR", collectorNumber = "258")
public class SoulSeparator extends Card {

    public SoulSeparator() {
        // {5}, {T}, Sacrifice this artifact: Exile target creature card from your graveyard. Create a
        // token that's a copy of that card, except it's 1/1, it's a Spirit in addition to its other
        // types, and it has flying. Create a black Zombie creature token with power equal to that
        // card's power and toughness equal to that card's toughness.
        addActivatedAbility(new ActivatedAbility(
                true, "{5}",
                List.of(new SacrificeSelfCost(),
                        ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(
                                new CardTypePredicate(CardType.CREATURE)),
                        CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                                new CreateTokenCopyOfTargetPermanentEffect(List.of(CardSubtype.SPIRIT), Set.of(), 1, 1,
                                        Map.of(), false, false, false, false, false, false, null,
                                        Set.of(Keyword.FLYING))),
                        new CreateTokenEffect("Zombie", AmountThisWay.exiledPower().keepingNegative(),
                                AmountThisWay.exiledToughness().keepingNegative(), CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())),
                "{5}, {T}, Sacrifice Soul Separator: Exile target creature card from your graveyard. "
                        + "Create a token that's a copy of that card, except it's 1/1, it's a Spirit in addition to "
                        + "its other types, and it has flying. Create a black Zombie creature token with power equal "
                        + "to that card's power and toughness equal to that card's toughness."));
    }
}
