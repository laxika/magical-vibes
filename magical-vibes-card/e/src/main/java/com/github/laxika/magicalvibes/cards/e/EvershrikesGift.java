package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "15")
public class EvershrikesGift extends Card {

    public EvershrikesGift() {
        // Enchant creature. Enchanted creature gets +1/+0 and has flying.
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));

        // {1}{W}, Blight 2: Return this card from your graveyard to your hand. Activate only as a
        // sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 2),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{1}{W}, Blight 2: Return this card from your graveyard to your hand. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
