package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "UDS", collectorNumber = "76")
public class AetherSting extends Card {

    public AetherSting() {
        // Whenever an opponent casts a creature spell, this enchantment deals 1 damage to that player.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastDamageToCasterEffect(1, new CardTypePredicate(CardType.CREATURE)));
    }
}
