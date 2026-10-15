package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureAndAllWithSameNameEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "173")
public class IzzetStaticaster extends Card {

    public IzzetStaticaster() {
        // Flash and Haste come from Scryfall keywords.
        // {T}: This creature deals 1 damage to target creature and each other creature with the same name as that creature.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetCreatureAndAllWithSameNameEffect(1)),
                "{T}: This creature deals 1 damage to target creature and each other creature with the same name as that creature."));
    }
}
