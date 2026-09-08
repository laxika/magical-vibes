package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "199")
public class EldraziMonument extends Card {

    public EldraziMonument() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE), GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice a creature", false),
                        List.of(new SacrificeSelfEffect())));
    }
}
