package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

import java.util.List;

/**
 * Storm Spirit — {3}{G}{W}{U} Creature — Elemental Spirit (3/3).
 * Flying (auto-loaded from Scryfall).
 * {T}: This creature deals 2 damage to target creature.
 */
@CardRegistration(set = "ICE", collectorNumber = "303")
public class StormSpirit extends Card {

    public StormSpirit() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{T}: Storm Spirit deals 2 damage to target creature."
        ));
    }
}
