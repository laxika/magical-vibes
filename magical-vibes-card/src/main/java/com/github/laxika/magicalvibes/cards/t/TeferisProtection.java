package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SLD", collectorNumber = "164")
@CardRegistration(set = "SLD", collectorNumber = "1691")
public class TeferisProtection extends Card {

    public TeferisProtection() {
        addEffect(EffectSlot.SPELL,
                new GrantStaticEffectToPlayerUntilNextTurnEffect(new LifeTotalCantChangeEffect()));
        addEffect(EffectSlot.SPELL, new GrantProtectionFromEverythingUntilNextTurnEffect());
        addEffect(EffectSlot.SPELL, new PhaseOutPermanentsEffect(new PermanentTruePredicate(), true));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
