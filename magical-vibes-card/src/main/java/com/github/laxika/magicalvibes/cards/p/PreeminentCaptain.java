package com.github.laxika.magicalvibes.cards.p;

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

@CardRegistration(set = "MOR", collectorNumber = "20")
public class PreeminentCaptain extends Card {

    public PreeminentCaptain() {
        // First strike is auto-loaded from Scryfall.
        // Whenever this creature attacks, you may put a Soldier creature card from your hand
        // onto the battlefield tapped and attacking. (Declinable via the card choice.)
        addEffect(EffectSlot.ON_ATTACK, PutCardToBattlefieldEffect.tappedAndAttacking(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.SOLDIER))),
                "Soldier creature"));
    }
}
