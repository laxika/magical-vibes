package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

/**
 * Desolate Lighthouse.
 * Land.
 * {T}: Add {C}.
 * {1}{U}{R}, {T}: Draw a card, then discard a card.
 */
@CardRegistration(set = "AVR", collectorNumber = "227")
public class DesolateLighthouse extends Card {

    public DesolateLighthouse() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        // {1}{U}{R}, {T}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{U}{R}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}{U}{R}, {T}: Draw a card, then discard a card."
        ));
    }
}
