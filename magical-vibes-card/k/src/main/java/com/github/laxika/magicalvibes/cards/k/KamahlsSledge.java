package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "102")
public class KamahlsSledge extends Card {

    public KamahlsSledge() {
        DealDamageEffect creatureDamage = DealDamageEffect.toTargetCreature(4);
        ConditionalEffect thresholdControllerDamage = new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 4));
        target(TargetFilters.creature())
                .bindEffect(creatureDamage)
                .bindEffect(thresholdControllerDamage);
        addSimultaneousSpellEffects(creatureDamage, thresholdControllerDamage);
    }
}
