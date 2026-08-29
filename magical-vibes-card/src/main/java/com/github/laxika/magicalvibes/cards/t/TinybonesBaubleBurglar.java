package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "72")
public class TinybonesBaubleBurglar extends Card {

    public TinybonesBaubleBurglar() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                ExileDiscardedCardFromGraveyardEffect.withStashCounter());
        addEffect(EffectSlot.STATIC,
                AllowCastFromCardsExiledWithSourceEffect.forStashCounters(true));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}",
                List.of(new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT)),
                "{3}{B}, {T}: Each opponent discards a card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
