package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "96")
@CardRegistration(set = "RVR", collectorNumber = "112")
public class HomingLightning extends Card {

    public HomingLightning() {
        // Homing Lightning deals 4 damage to target creature and each other creature with the same name as that creature.
        DealDamageEffect targetDamage = DealDamageEffect.toTargetCreature(4);
        target(TargetFilters.creature()).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage, DealDamageEffect.toEach(
                Subjects.otherCreaturesSharing(Subjects.target(), SharedQuality.NAME),
                4));
    }
}
