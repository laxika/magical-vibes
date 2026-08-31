package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureInsteadOfNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "65")
public class KillSuitCultist extends Card {

    public KillSuitCultist() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeSelfCost(), new DestroyTargetCreatureInsteadOfNextDamageEffect()),
                "{B}, Sacrifice this creature: The next time damage would be dealt to target creature this turn, destroy that creature instead.",
                TargetFilters.creature()
        ));
    }
}
