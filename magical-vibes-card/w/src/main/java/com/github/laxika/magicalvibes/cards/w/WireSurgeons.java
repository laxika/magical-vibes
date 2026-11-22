package com.github.laxika.magicalvibes.cards.w;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect.PerCard;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BRC", collectorNumber = "10")
@CardRegistration(set = "BRC", collectorNumber = "57")
public class WireSurgeons extends Card {

    public WireSurgeons() {
        addStaticEffect(new GrantGraveyardAbilityToCardsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE))),
                PerCard.ENCORE_FOR_MANA_COST));
    }
}
