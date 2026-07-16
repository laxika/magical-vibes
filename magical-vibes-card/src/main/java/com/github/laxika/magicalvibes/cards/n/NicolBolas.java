package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "DRB", collectorNumber = "10")
public class NicolBolas extends Card {

    public NicolBolas() {
        // At the beginning of your upkeep, sacrifice Nicol Bolas unless you pay {U}{B}{R}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{U}{B}{R}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Whenever Nicol Bolas deals damage to an opponent, that player discards their hand.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new DiscardHandEffect(DiscardRecipient.TARGET_PLAYER));
    }
}
