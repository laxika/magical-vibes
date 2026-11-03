package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "87")
@CardRegistration(set = "SPM", collectorNumber = "262")
@CardRegistration(set = "OM1", collectorNumber = "90")
public class ShadowOfTheGoblin extends Card {

    public ShadowOfTheGoblin() {
        // At the beginning of your first main phase, discard a card. If you do, draw a card.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new DiscardAndDrawCardEffect());

        // Whenever you play a land or cast a spell from anywhere other than your hand, this
        // enchantment deals 1 damage to each opponent.
        List<CardEffect> damage = List.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND), damage.toArray(CardEffect[]::new));
        addTrigger(Triggers.youPlayLand().notPlayedFrom(Zone.HAND), damage.toArray(CardEffect[]::new));
    }
}
