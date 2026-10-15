package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "176")
public class KumanoMasterYamabushi extends Card {

    public KumanoMasterYamabushi() {
        // "{1}{R}: Kumano deals 1 damage to any target."
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new DealDamageToAnyTargetEffect(1)),
                "{1}{R}: Kumano deals 1 damage to any target."));

        // "If a creature dealt damage by Kumano this turn would die, exile it instead."
        addEffect(EffectSlot.STATIC, new ExileCreaturesDamagedBySourceInsteadOfDyingEffect());
    }
}
