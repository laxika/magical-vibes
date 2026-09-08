package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "9")
public class Candletrap extends Card {

    public Candletrap() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new PreventAllDamageDealtByEnchantedCreatureEffect(true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new SacrificeSelfCost(), new ExileEnchantedCreatureEffect()),
                "Coven — {2}{W}, Sacrifice this Aura: Exile enchanted creature. Activate only if you control "
                        + "three or more creatures with different powers.",
                ActivationTimingRestriction.COVEN
        ));
    }
}
