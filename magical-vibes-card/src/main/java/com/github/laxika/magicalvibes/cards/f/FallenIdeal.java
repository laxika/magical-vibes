package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "109")
public class FallenIdeal extends Card {

    public FallenIdeal() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                null,
                                List.of(new SacrificeCreatureCost(), new BoostSelfEffect(2, 1)),
                                "Sacrifice a creature: This creature gets +2/+1 until end of turn."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build());
    }
}
