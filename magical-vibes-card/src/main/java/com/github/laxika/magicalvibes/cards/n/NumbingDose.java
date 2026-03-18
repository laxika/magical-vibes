package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "40")
public class NumbingDose extends Card {

    public NumbingDose() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be an artifact or creature"
        ))
                // Enchanted permanent doesn't untap during its controller's untap step.
                .addEffect(EffectSlot.STATIC, new AttachedCreatureDoesntUntapEffect())

                // At the beginning of the upkeep of enchanted permanent's controller, that player loses 1 life.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED, new EnchantedCreatureControllerLosesLifeEffect(1));
    }
}
