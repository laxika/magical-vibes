package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "15")
@CardRegistration(set = "E01", collectorNumber = "13")
public class GraspOfTheHieromancer extends Card {

    public GraspOfTheHieromancer() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature gets +1/+1
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                // ... and has "Whenever this creature attacks, tap target creature defending player
                // controls." The aura's Triggers.attacks() ability triggers on the enchanted creature's attack
                // (AttackTriggers); the effect's own target predicate narrows the choice to the defending
                // player's creatures.
                .addTrigger(Triggers.attacks(), new TapPermanentsEffect(
                        TapUntapScope.TARGET,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledByDefendingPlayerPredicate()))));
    }
}
