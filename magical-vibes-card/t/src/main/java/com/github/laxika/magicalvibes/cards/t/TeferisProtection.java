package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SLD", collectorNumber = "164")
@CardRegistration(set = "SLD", collectorNumber = "1691")
@CardRegistration(set = "2X2", collectorNumber = "32")
@CardRegistration(set = "STA", collectorNumber = "11")
@CardRegistration(set = "TLE", collectorNumber = "7")
@CardRegistration(set = "MAR", collectorNumber = "51")
@CardRegistration(set = "C17", collectorNumber = "8")
public class TeferisProtection extends Card {

    public TeferisProtection() {
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilYourNextTurn(new LifeTotalCantChangeEffect()));
        addSpellEffect(new GrantProtectionFromEverythingUntilNextTurnEffect());
        addSpellEffect(new PhaseOutEffect(Subjects.controlledBy(Subjects.you(), new PermanentTruePredicate())));
        addSpellEffect(new ExileSpellEffect());
    }
}
