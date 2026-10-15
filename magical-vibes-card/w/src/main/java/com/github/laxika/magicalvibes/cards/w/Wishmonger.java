package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "57")
public class Wishmonger extends Card {

    public Wishmonger() {
        // {2}: Target creature gains protection from the color of its controller's choice until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(false, true)),
                "{2}: Target creature gains protection from the color of its controller's choice until end of turn. Any player may activate this ability.",
                TargetFilters.creature()
        ).withActivatableByAnyPlayer());
    }
}
