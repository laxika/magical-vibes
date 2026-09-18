package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "2X2", collectorNumber = "32")
@CardRegistration(set = "STA", collectorNumber = "11")
@CardRegistration(set = "TLE", collectorNumber = "7")
@CardRegistration(set = "MAR", collectorNumber = "51")
public class TeferisProtection extends Card {

    public TeferisProtection() {
        addEffect(EffectSlot.SPELL, new LifeTotalCantChangeUntilNextTurnEffect());
        addEffect(EffectSlot.SPELL, new GrantProtectionFromEverythingUntilNextTurnEffect());
        addEffect(EffectSlot.SPELL, new PhaseOutPermanentsEffect(new PermanentTruePredicate(), true));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
