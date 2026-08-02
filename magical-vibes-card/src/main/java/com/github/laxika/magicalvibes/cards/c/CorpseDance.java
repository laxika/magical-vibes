package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Corpse Dance — {@code {2}{B}} instant.
 *
 * <p>"Buyback {2}. Return the top creature card of your graveyard to the battlefield. That creature
 * gains haste until end of turn. Exile it at the beginning of the next end step."
 *
 * <p>Shallow Grave's reanimation plus buyback. Non-targeting: {@code topmost} forces the most
 * recently placed creature card, so no prompt is needed.
 */
@CardRegistration(set = "TMP", collectorNumber = "116")
public class CorpseDance extends Card {

    public CorpseDance() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{2}"));
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .topmost(true)
                .grantHaste(true)
                .exileAtEndStep(true)
                .build());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
