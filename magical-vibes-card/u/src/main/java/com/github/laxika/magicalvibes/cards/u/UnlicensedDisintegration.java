package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "187")
@CardRegistration(set = "2XM", collectorNumber = "224")
@CardRegistration(set = "KLR", collectorNumber = "211")
@CardRegistration(set = "SCD", collectorNumber = "255")
public class UnlicensedDisintegration extends Card {

    public UnlicensedDisintegration() {
        // Destroy target creature. If you control an artifact, Unlicensed Disintegration deals 3
        // damage to that creature's controller.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()), DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 3)));
    }
}
