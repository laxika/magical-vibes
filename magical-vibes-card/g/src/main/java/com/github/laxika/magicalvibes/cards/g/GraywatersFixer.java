package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect.PerCard;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "36")
@CardRegistration(set = "OTC", collectorNumber = "72")
public class GraywatersFixer extends Card {

    public GraywatersFixer() {
        addStaticEffect(new GrantGraveyardAbilityToCardsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ASSASSIN),
                                new CardSubtypePredicate(CardSubtype.MERCENARY),
                                new CardSubtypePredicate(CardSubtype.PIRATE),
                                new CardSubtypePredicate(CardSubtype.ROGUE),
                                new CardSubtypePredicate(CardSubtype.WARLOCK))))),
                PerCard.ENCORE_FOR_MANA_VALUE));
    }
}
