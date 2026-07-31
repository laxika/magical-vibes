package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "84a")
@CardRegistration(set = "ALL", collectorNumber = "84b")
public class VeteransVoice extends Card {

    public VeteransVoice() {
        // Enchant creature you control.
        target(TargetFilters.creatureYouControl());

        // Tap enchanted creature: Target creature other than the creature tapped this way
        // gets +2/+1 until end of turn. Activate only if enchanted creature is untapped.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new TapEnchantedPermanentCost(), new BoostTargetCreatureEffect(2, 1)),
                "Tap enchanted creature: Target creature other than the creature tapped this way "
                        + "gets +2/+1 until end of turn. Activate only if enchanted creature is untapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate()))),
                        "Target must be a creature other than enchanted creature")));
    }
}
