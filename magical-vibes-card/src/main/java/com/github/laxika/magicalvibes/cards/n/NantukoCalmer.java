package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "132")
public class NantukoCalmer extends Card {

    public NantukoCalmer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{G}, {T}, Sacrifice Nantuko Calmer: Destroy target enchantment.",
                TargetFilters.enchantment()
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));
    }
}
