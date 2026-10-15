package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;

@CardRegistration(set = "BLB", collectorNumber = "161")
public class WhiskerquillScribe extends Card {

    public WhiskerquillScribe() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new TriggeringSpellControllerConditionalEffect(new OncePerTurnTriggerEffect(
                        new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"))));
    }
}
