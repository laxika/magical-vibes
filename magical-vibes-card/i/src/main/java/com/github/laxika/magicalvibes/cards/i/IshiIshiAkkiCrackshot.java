package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Ishi-Ishi, Akki Crackshot — whenever an opponent casts a Spirit or Arcane spell,
 * deals 2 damage to that player.
 */
@CardRegistration(set = "BOK", collectorNumber = "110")
public class IshiIshiAkkiCrackshot extends Card {

    public IshiIshiAkkiCrackshot() {
        // Whenever an opponent casts a Spirit or Arcane spell, Ishi-Ishi deals 2 damage to that player.
        addTrigger(Triggers.opponentCastsSpell(), new SpellCastDamageToCasterEffect(2,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new CardSubtypePredicate(CardSubtype.ARCANE)))));
    }
}
