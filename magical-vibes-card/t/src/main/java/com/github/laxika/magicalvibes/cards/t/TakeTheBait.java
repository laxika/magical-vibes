package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MKC", collectorNumber = "43")
@CardRegistration(set = "MKC", collectorNumber = "353")
public class TakeTheBait extends Card {

    public TakeTheBait() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_COMBAT);
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatToControllerAndPlaneswalkers());
        addEffect(EffectSlot.SPELL,
                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.SPELL,
                new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.SPELL, new AdditionalCombatPhaseEffect(1));
    }
}
