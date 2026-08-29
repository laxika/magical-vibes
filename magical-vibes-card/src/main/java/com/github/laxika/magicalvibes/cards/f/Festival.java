package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "DRK", collectorNumber = "8")
public class Festival extends Card {

    public Festival() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_UPKEEP);
        addEffect(EffectSlot.SPELL, new CantAttackThisTurnEffect(TapUntapScope.ALL_CREATURES));
    }
}
