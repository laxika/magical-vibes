package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "27")
public class PontiffOfBlight extends Card {

    public PontiffOfBlight() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, extort());
        // "Other creatures you control have extort" — OWN_CREATURES excludes the source, which
        // already has its own printed extort above. Each instance triggers separately.
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL, extort(), GrantScope.OWN_CREATURES));
    }

    private static MayEffect extort() {
        return new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                        "{W/B}"
                ),
                "Pay {W/B} to extort?"
        );
    }
}
