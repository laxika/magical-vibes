package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "14")
public class RoyalDecree extends Card {

    private static final PermanentPredicate SWAMP_MOUNTAIN_BLACK_OR_RED = new PermanentAnyOfPredicate(List.of(
            new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SWAMP, CardSubtype.MOUNTAIN)),
            new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED))));

    public RoyalDecree() {
        // Cumulative upkeep {W}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{W}"));

        // Whenever a Swamp, Mountain, black permanent, or red permanent becomes tapped, this
        // enchantment deals 1 damage to that permanent's controller. Both ally- and opponent-
        // scoped tap slots are needed; the conditional filters the tapped permanent. One trigger
        // per tap even if the permanent matches multiple criteria (e.g. Badlands).
        var damage = new TriggeringPermanentConditionalEffect(
                SWAMP_MOUNTAIN_BLACK_OR_RED,
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, damage);
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, damage);
    }
}
