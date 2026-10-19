package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "72")
public class StringOfDisappearances extends Card {

    public StringOfDisappearances() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target())
                .addEffect(EffectSlot.SPELL, new MayPayManaEffect(
                        "{U}{U}",
                        new MayEffect(new CopyThisSpellForTargetControllerEffect(),
                                "Copy String of Disappearances?", null, MayChoicePlayer.LAST_MANA_PAYMENT_PLAYER),
                        "Pay {U}{U} to copy String of Disappearances?",
                        MayPayPayer.TARGET_PERMANENT_CONTROLLER));
    }
}
