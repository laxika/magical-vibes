package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "95")
public class DemonicEmbrace extends Card {

    public DemonicEmbrace() {
        addCastingOption(new GraveyardCast(List.of(
                new LifeCastingCost(3),
                new DiscardCardCastingCost())));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(3, 1, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.DEMON, GrantScope.ENCHANTED_CREATURE));
    }
}
