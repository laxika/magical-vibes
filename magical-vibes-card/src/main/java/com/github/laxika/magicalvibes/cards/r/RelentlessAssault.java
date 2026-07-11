package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;

@CardRegistration(set = "10E", collectorNumber = "225")
@CardRegistration(set = "9ED", collectorNumber = "212")
@CardRegistration(set = "P02", collectorNumber = "115")
@CardRegistration(set = "PTK", collectorNumber = "119")
public class RelentlessAssault extends Card {

    public RelentlessAssault() {
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES));
        addEffect(EffectSlot.SPELL, new AdditionalCombatMainPhaseEffect(1));
    }
}
