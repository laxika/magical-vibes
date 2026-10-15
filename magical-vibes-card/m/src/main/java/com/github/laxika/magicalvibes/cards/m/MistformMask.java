package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "94")
public class MistformMask extends Card {

    public MistformMask() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new EnchantedCreatureBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{1}: Enchanted creature becomes the creature type of your choice until end of turn."
        ));
    }
}
