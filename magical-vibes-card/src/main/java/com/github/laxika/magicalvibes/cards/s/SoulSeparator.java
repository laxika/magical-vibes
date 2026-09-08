package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "270")
@CardRegistration(set = "INR", collectorNumber = "446")
@CardRegistration(set = "EMN", collectorNumber = "199")
public class SoulSeparator extends Card {

    public SoulSeparator() {
        // {5}, {T}, Sacrifice this artifact: Exile target creature card from your graveyard. Create a
        // token that's a copy of that card, except it's 1/1, it's a Spirit in addition to its other
        // types, and it has flying. Create a black Zombie creature token with power equal to that
        // card's power and toughness equal to that card's toughness.
        addActivatedAbility(new ActivatedAbility(
                true, "{5}",
                List.of(new SacrificeSelfCost(),
                        new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                true,
                                List.of(CardSubtype.SPIRIT),
                                false,
                                false,
                                null,
                                1,
                                1,
                                Set.of(Keyword.FLYING),
                                true)),
                "{5}, {T}, Sacrifice Soul Separator: Exile target creature card from your graveyard. "
                        + "Create a token that's a copy of that card, except it's 1/1, it's a Spirit in addition to "
                        + "its other types, and it has flying. Create a black Zombie creature token with power equal "
                        + "to that card's power and toughness equal to that card's toughness."));
    }
}
