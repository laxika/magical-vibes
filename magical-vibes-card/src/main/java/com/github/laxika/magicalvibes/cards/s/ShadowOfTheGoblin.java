package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "87")
public class ShadowOfTheGoblin extends Card {

    public ShadowOfTheGoblin() {
        // At the beginning of your first main phase, discard a card. If you do, draw a card.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new DiscardAndDrawCardEffect());

        // Whenever you play a land or cast a spell from anywhere other than your hand, this
        // enchantment deals 1 damage to each opponent.
        List<CardEffect> damage = List.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new PlayFromOutsideHandTriggerEffect(damage));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new PlayFromOutsideHandTriggerEffect(damage));
    }
}
