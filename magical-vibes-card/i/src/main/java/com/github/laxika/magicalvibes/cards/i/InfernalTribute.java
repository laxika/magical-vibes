package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Infernal Tribute — {2}, Sacrifice a nontoken permanent: Draw a card.
 *
 * <p>The sacrifice is not restricted to other permanents; Infernal Tribute itself is a nontoken
 * permanent and may be sacrificed to its own ability, so the cost does not exclude the source.
 */
@CardRegistration(set = "WTH", collectorNumber = "73")
public class InfernalTribute extends Card {

    public InfernalTribute() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SacrificePermanentCost(new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                "Sacrifice a nontoken permanent", false),
                        new DrawCardEffect(1)),
                "{2}, Sacrifice a nontoken permanent: Draw a card."));
    }
}
