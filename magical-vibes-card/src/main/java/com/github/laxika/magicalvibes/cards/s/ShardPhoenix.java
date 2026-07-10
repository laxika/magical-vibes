package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "217")
public class ShardPhoenix extends Card {

    public ShardPhoenix() {
        // Sacrifice this creature: It deals 2 damage to each creature without flying.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(),
                        new MassDamageEffect(2, false, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                "Sacrifice Shard Phoenix: It deals 2 damage to each creature without flying."
        ));

        // {R}{R}{R}: Return Shard Phoenix from your graveyard to your hand. Activate only during your upkeep.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{R}{R}{R}: Return Shard Phoenix from your graveyard to your hand. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
