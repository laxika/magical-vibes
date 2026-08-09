package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "23")
public class WarriorEnKor extends Card {

    public WarriorEnKor() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new RedirectNextDamageEffect(RedirectRole.SOURCE_PERMANENT, RedirectRole.TARGET,
                        1, TargetPredicates.creature())),
                "{0}: The next 1 damage that would be dealt to this creature this turn is dealt to target "
                        + "creature you control instead.",
                TargetFilters.creatureYouControl()));
    }
}
