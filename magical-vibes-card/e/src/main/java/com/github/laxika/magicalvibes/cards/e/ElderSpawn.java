package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "52")
public class ElderSpawn extends Card {

    public ElderSpawn() {
        // At the beginning of your upkeep, unless you sacrifice an Island, sacrifice this creature
        // and it deals 6 damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new SacrificePermanentCost(
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND), "Sacrifice an Island"),
                List.of(
                        new SacrificeSelfEffect(),
                        new DealDamageToPlayersEffect(6, DamageRecipient.CONTROLLER)),
                true));

        // This creature can't be blocked by red creatures.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentColorInPredicate(Set.of(CardColor.RED))));
    }
}
