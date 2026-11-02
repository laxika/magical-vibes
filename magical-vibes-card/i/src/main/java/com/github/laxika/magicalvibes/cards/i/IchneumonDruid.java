package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "191")
public class IchneumonDruid extends Card {

    public IchneumonDruid() {
        // Whenever an opponent casts an instant spell other than the first instant spell that
        // player casts each turn, this creature deals 4 damage to that player.
        addTrigger(Triggers.opponentCastsSpell(), SpellCastTriggerEffect.atLeast(
                2,
                new CardTypePredicate(CardType.INSTANT),
                List.of(new DealDamageToPlayersEffect(4, DamageRecipient.TRIGGERING_PLAYER))));
    }
}
