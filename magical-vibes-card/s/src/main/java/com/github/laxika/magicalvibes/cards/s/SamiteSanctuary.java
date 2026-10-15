package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "21")
public class SamiteSanctuary extends Card {

    public SamiteSanctuary() {
        // {2}: Prevent the next 1 damage that would be dealt to target creature this turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(PreventDamageEffect.nextToTargetCreature(1)),
                "{2}: Prevent the next 1 damage that would be dealt to target creature this turn. Any player may activate this ability.",
                TargetFilters.creature()
        ).withActivatableByAnyPlayer());
    }
}
