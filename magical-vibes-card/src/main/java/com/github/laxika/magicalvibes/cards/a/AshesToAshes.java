package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "141")
public class AshesToAshes extends Card {

    public AshesToAshes() {
        // Exile two target nonartifact creatures. Ashes to Ashes deals 5 damage to you.
        // Two distinct target groups (shared targets not allowed) so the two
        // nonartifact creatures must differ, each exiled by its own effect.
        target(nonartifactCreatureFilter("First target must be a nonartifact creature"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
        target(nonartifactCreatureFilter("Second target must be a nonartifact creature"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());

        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(5, DamageRecipient.CONTROLLER));
    }

    private static PermanentPredicateTargetFilter nonartifactCreatureFilter(String description) {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate())
                )),
                description);
    }
}
