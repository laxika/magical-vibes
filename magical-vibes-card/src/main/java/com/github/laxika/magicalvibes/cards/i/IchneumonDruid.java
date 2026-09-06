package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "191")
public class IchneumonDruid extends Card {

    public IchneumonDruid() {
        // Whenever an opponent casts an instant spell other than the first instant spell that
        // player casts each turn, this creature deals 4 damage to that player.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, SpellCastTriggerEffect.atLeast(
                2,
                new CardTypePredicate(CardType.INSTANT),
                List.of(new DealDamageToPlayersEffect(4, DamageRecipient.TRIGGERING_PLAYER))));
    }
}
