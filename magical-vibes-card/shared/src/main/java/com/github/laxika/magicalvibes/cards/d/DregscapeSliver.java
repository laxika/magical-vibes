package com.github.laxika.magicalvibes.cards.d;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "631")
@CardRegistration(set = "MH1", collectorNumber = "88")
public class DregscapeSliver extends Card {

    public DregscapeSliver() {
        addStaticEffect(new GrantGraveyardAbilityToCardsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.SLIVER))),
                Card.unearthAbility("{2}")));
        addUnearth("{2}");
    }
}
