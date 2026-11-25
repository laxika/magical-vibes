package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "72")
public class StringOfDisappearances extends Card {

    public StringOfDisappearances() {
        target(TargetFilters.creature())
                .addSpellEffect(ReturnToHandEffect.target())
                .addSpellEffect(UnlessPaysEffect.mayPay(
                        Subjects.controllerOf(Subjects.target()),
                        "{U}{U}",
                        new MayEffect(new CopyThisSpellForTargetControllerEffect(),
                                "Copy String of Disappearances?", null, MayChoicePlayer.TARGET_PERMANENT_CONTROLLER))
                        .withPrompt("Pay {U}{U} to copy String of Disappearances?"));
    }
}
