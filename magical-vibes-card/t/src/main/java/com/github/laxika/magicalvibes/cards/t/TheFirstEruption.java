package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * The First Eruption — {2}{R} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — The First Eruption deals 1 damage to each creature without flying.
 * II — Add {R}{R}.
 * III — Sacrifice a Mountain. If you do, The First Eruption deals 3 damage to each creature.
 */
@CardRegistration(set = "DOM", collectorNumber = "122")
public class TheFirstEruption extends Card {

    public TheFirstEruption() {
        // Chapter I: deal 1 damage to each creature without flying
        addTrigger(Triggers.chapter(1), new MassDamageEffect(
                1, false, false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
        ));

        // Chapter II: add {R}{R}
        addTrigger(Triggers.chapter(2), new AwardManaEffect(ManaColor.RED, 2));

        // Chapter III: sacrifice a Mountain; if you do, deal 3 damage to each creature
        addTrigger(Triggers.chapter(3), new SacrificePermanentThenEffect(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                new MassDamageEffect(3),
                "a Mountain",
                false,
                false
        ));
    }
}
