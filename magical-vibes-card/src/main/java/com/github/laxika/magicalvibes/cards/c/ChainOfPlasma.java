package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "193")
public class ChainOfPlasma extends Card {

    public ChainOfPlasma() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new MayEffect(
                                new CopyThisSpellForTargetPlayerEffect(),
                                "Copy Chain of Plasma?",
                                null,
                                MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER
                        ),
                        "a card",
                        DiscardRecipient.TARGET_PLAYER_OR_PERMANENT_CONTROLLER,
                        true
                ),
                "Discard a card?",
                null,
                MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER
        ));
    }
}
