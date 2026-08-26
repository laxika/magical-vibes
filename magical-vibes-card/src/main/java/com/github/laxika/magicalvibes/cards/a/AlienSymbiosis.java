package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "50")
public class AlienSymbiosis extends Card {

    public AlienSymbiosis() {
        addCastingOption(new GraveyardCast(List.of(new DiscardCardCastingCost())));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(1, 1, Set.of(Keyword.MENACE), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.SYMBIOTE, GrantScope.ENCHANTED_CREATURE));
    }
}
