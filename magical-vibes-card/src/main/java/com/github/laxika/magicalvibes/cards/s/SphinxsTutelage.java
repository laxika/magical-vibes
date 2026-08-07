package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentRepeatOnSharedColorEffect;

import java.util.List;

/**
 * Sphinx's Tutelage — Enchantment.
 * Whenever you draw a card, target opponent mills two cards. If two nonland cards that share a
 * color were milled this way, repeat this process.
 * {5}{U}: Draw a card, then discard a card.
 */
@CardRegistration(set = "ORI", collectorNumber = "76")
public class SphinxsTutelage extends Card {

    public SphinxsTutelage() {
        // Whenever you draw a card, target opponent mills two cards. If two nonland cards that
        // share a color were milled this way, repeat this process. The two-player engine derives
        // the sole opponent, so no target is chosen.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new MillOpponentRepeatOnSharedColorEffect(2));

        // {5}{U}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                false, "{5}{U}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{5}{U}: Draw a card, then discard a card."
        ));
    }
}
