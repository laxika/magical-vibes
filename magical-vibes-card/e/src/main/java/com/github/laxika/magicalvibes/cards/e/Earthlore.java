package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "231")
public class Earthlore extends Card {

    public Earthlore() {
        // Enchant land you control.
        target(TargetFilters.landYouControl());
        // Tap enchanted land: Target blocking creature gets +1/+2 until end of turn.
        // Activate only if enchanted land is untapped.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new TapEnchantedPermanentCost(), new BoostTargetCreatureEffect(1, 2)),
                "Tap enchanted land: Target blocking creature gets +1/+2 until end of turn. "
                        + "Activate only if enchanted land is untapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsBlockingPredicate(),
                        "Target must be a blocking creature"
                )));
    }
}
