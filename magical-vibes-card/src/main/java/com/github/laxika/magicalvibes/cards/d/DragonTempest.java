package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "136")
public class DragonTempest extends Card {

    public DragonTempest() {
        // Whenever a creature you control with flying enters, it gains haste until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE))));

        // Whenever a Dragon you control enters, this enchantment deals X damage to any target,
        // where X is the number of Dragons you control.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON),
                        new DealDamageToAnyTargetEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON), CountScope.CONTROLLER))));
    }
}
