package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "142")
@CardRegistration(set = "ME3", collectorNumber = "95")
public class Disharmony extends Card {

    public Disharmony() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT_BEFORE_BLOCKERS);

        target(TargetFilters.attackingCreature())
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new RemoveTargetFromCombatEffect())
                .addSpellEffect(new GainControlOfTargetEffect(ControlDuration.END_OF_TURN));
    }
}
