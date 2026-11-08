package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "187")
public class DwarvenShrine extends Card {

    public DwarvenShrine() {
        // Whenever a player casts a spell, this enchantment deals twice the number of matching
        // cards in all graveyards in damage to that player.
        addTrigger(Triggers.playerCastsSpell().anySpell(), new DealDamageToPlayersEffect(
                new Scaled(new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER), 2),
                DamageRecipient.TRIGGERING_PLAYER));
    }
}
