package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.IllusionistsGambitEffect;

@CardRegistration(set = "C13", collectorNumber = "47")
public class IllusionistsGambit extends Card {

    public IllusionistsGambit() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_DECLARE_BLOCKERS);
        addEffect(EffectSlot.SPELL, new IllusionistsGambitEffect());
    }
}
