package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "12")
public class DetainmentSpell extends Card {

    public DetainmentSpell() {
        target(TargetFilters.creature())
                .addStaticEffect(EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new AttachSourceEquipmentToTargetCreatureEffect()),
                "{1}{W}: Attach this Aura to target creature.",
                TargetFilters.creature()
        ));
    }
}
