package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "290")
@CardRegistration(set = "5ED", collectorNumber = "321")
@CardRegistration(set = "9ED", collectorNumber = "265")
@CardRegistration(set = "8ED", collectorNumber = "275")
@CardRegistration(set = "7ED", collectorNumber = "265")
@CardRegistration(set = "6ED", collectorNumber = "248")
public class Regeneration extends Card {

    public Regeneration() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate enchanted creature."
        ));
    }
}
