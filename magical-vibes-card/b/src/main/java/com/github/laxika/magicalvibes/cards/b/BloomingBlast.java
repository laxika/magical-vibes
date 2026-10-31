package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "126")
public class BloomingBlast extends Card {

    public BloomingBlast() {
        addEffect(EffectSlot.STATIC, new GiftEffect());
        target(TargetFilters.creature())
                .addSpellEffect(new ConditionalEffect(new GiftPromised(),
                        TargetOpponentCreatesTokenEffect.gift(CreateTokenEffect.ofTreasureToken(1))))
                .addSpellEffect(new DealDamageToTargetCreatureEffect(2))
                .addSpellEffect(new ConditionalEffect(new GiftPromised(),
                        new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
