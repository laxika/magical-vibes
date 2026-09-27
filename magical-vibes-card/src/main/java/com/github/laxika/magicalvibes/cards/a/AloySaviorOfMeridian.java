package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2221")
public class AloySaviorOfMeridian extends Card {

    public AloySaviorOfMeridian() {
        // Whenever one or more artifact creatures you control attack, discover X, where X is the
        // greatest power among them.
        var artifactAttacker = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new HasAttacker(new PermanentIsArtifactPredicate()),
                        new DiscoverEffect(new GreatestPowerAmongControlled(artifactAttacker))));
    }
}
