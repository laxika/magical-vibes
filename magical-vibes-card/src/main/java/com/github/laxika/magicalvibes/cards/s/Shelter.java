package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "46")
@CardRegistration(set = "EMA", collectorNumber = "29")
@CardRegistration(set = "VMA", collectorNumber = "44")
@CardRegistration(set = "SLD", collectorNumber = "1587")
@CardRegistration(set = "MH1", collectorNumber = "28")
public class Shelter extends Card {

    public Shelter() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
