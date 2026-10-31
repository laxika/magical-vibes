package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        addSpellEffect(PreventDamageEffect.allCombatToControllerAndPlaneswalkers());
        addSpellEffect(new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate()));
        addSpellEffect(new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentIsAttackingPredicate()));
        addSpellEffect(new AdditionalCombatPhaseEffect(1));
    }
}
