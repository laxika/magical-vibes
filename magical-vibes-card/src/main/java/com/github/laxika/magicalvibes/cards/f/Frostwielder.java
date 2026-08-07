package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "167")
public class Frostwielder extends Card {

    public Frostwielder() {
        // "If a creature dealt damage by this creature this turn would die, exile it instead."
        addEffect(EffectSlot.STATIC, new ExileCreaturesDamagedBySourceInsteadOfDyingEffect());

        // "{T}: This creature deals 1 damage to any target."
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Frostwielder deals 1 damage to any target."));
    }
}
