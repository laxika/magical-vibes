package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "15")
public class GraspOfTheHieromancer extends Card {

    public GraspOfTheHieromancer() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature gets +1/+1
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                // ... and has "Whenever this creature attacks, tap target creature defending player
                // controls." The aura's ON_ATTACK slot is scanned for the attacking creature by
                // CombatTriggerService.checkAuraTriggersForCreature; the effect's own target
                // predicate narrows the choice to the defending player's creatures.
                .addEffect(EffectSlot.ON_ATTACK, new TapPermanentsEffect(
                        TapUntapScope.TARGET,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledByDefendingPlayerPredicate()))));
    }
}
