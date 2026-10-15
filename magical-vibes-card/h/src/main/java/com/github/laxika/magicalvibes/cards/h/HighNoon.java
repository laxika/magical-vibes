package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "15")
public class HighNoon extends Card {

    public HighNoon() {
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1, SpellLimitScope.EACH_PLAYER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(5)),
                "{4}{R}, Sacrifice this enchantment: It deals 5 damage to any target."
        ));
    }
}
