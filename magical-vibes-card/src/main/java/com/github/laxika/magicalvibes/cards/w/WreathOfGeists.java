package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ISD", collectorNumber = "211")
public class WreathOfGeists extends Card {

    public WreathOfGeists() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
        // Enchanted creature gets +X/+X, where X is the number of creature cards in your graveyard
        .addEffect(EffectSlot.STATIC, new BoostCreaturePerCardsInControllerGraveyardEffect(
                new CardTypePredicate(CardType.CREATURE),
                1, 1,
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
