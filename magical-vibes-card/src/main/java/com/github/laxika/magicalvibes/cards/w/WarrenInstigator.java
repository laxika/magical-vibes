package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "154")
public class WarrenInstigator extends Card {

    public WarrenInstigator() {
        // Double strike is auto-loaded from Scryfall.
        // Whenever this creature deals damage to an opponent, you may put a Goblin creature card
        // from your hand onto the battlefield. The hand-card choice is declinable.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new PutCardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.GOBLIN))),
                "Goblin creature"));
    }
}
