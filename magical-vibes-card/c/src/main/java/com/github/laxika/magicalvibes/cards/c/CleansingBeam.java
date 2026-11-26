package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "118")
@CardRegistration(set = "CMD", collectorNumber = "116")
@CardRegistration(set = "CMA", collectorNumber = "78")
public class CleansingBeam extends Card {

    public CleansingBeam() {
        DealDamageEffect targetDamage = DealDamageEffect.toTargetCreature(2);
        target(TargetFilters.creature()).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage, DealDamageEffect.toEach(
                Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.COLOR),
                2));
    }
}
