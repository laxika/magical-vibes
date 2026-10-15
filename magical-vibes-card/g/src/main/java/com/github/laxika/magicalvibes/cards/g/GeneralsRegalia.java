package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "295")
public class GeneralsRegalia extends Card {

    public GeneralsRegalia() {
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new RedirectNextDamageFromChosenSourceToTargetCreatureEffect()),
                "{3}: The next time a source of your choice would deal damage to you this turn, that damage is dealt to target creature you control instead.",
                TargetFilters.creatureYouControl()));
    }
}
