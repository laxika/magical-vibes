package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "51")
public class AuraOfDominion extends Card {

    public AuraOfDominion() {
        // Enchant creature
        target(TargetFilters.creature());

        // {1}, Tap an untapped creature you control: Untap enchanted creature.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{1}, Tap an untapped creature you control: Untap enchanted creature."));
    }
}
