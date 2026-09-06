package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "187")
public class GeneralKudroOfDrannith extends Card {

    public GeneralKudroOfDrannith() {
        var humanFilter = new PermanentHasSubtypePredicate(CardSubtype.HUMAN);
        var humanCreatureFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), humanFilter));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, humanFilter));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD,
                        null, null, true, false, false));

        var largeCreatureFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, humanCreatureFilter),
                        new DestroyTargetPermanentEffect(false)
                ),
                "{2}, Sacrifice two Humans: Destroy target creature with power 4 or greater.",
                new PermanentPredicateTargetFilter(largeCreatureFilter,
                        "Target must be a creature with power 4 or greater")
        ));
    }
}
