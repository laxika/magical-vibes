package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "28")
public class WardOfPiety extends Card {

    public WardOfPiety() {
        // Enchant creature.
        target(TargetFilters.creature());

        // {1}{W}: The next 1 damage that would be dealt to enchanted creature this turn is
        // dealt to any target instead.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new RedirectNextDamageEffect(RedirectRole.ENCHANTED_PERMANENT, RedirectRole.TARGET,
                        1, TargetPredicates.anyTarget())),
                "{1}{W}: The next 1 damage that would be dealt to enchanted creature this turn is dealt to any target instead."));
    }
}
