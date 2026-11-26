package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "5ED", collectorNumber = "127")
@CardRegistration(set = "4ED", collectorNumber = "104")
@CardRegistration(set = "SUM", collectorNumber = "83")
@CardRegistration(set = "3ED", collectorNumber = "83")
@CardRegistration(set = "SLD", collectorNumber = "1103")
@CardRegistration(set = "ME4", collectorNumber = "64")
@CardRegistration(set = "2ED", collectorNumber = "81")
@CardRegistration(set = "LEA", collectorNumber = "80")
@CardRegistration(set = "LEB", collectorNumber = "81")
public class Stasis extends Card {

    public Stasis() {
        // Players skip their untap steps.
        addStaticEffect(new SkipDrawStepEffect(SkipStepOrPhaseKind.UNTAP_STEP, Subjects.eachPlayer()));

        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {U}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{U}", new SacrificeSelfEffect()));
    }
}
