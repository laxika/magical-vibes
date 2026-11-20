package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherCreaturesSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "118")
@CardRegistration(set = "CMD", collectorNumber = "116")
@CardRegistration(set = "CMA", collectorNumber = "78")
public class CleansingBeam extends Card {

    public CleansingBeam() {
        DealDamageToTargetCreatureEffect targetDamage = new DealDamageToTargetCreatureEffect(2);
        target(TargetFilters.creature()).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage, new DealDamageToPermanentsTargetControlsEffect(2,
                Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.COLOR)));
    }
}
