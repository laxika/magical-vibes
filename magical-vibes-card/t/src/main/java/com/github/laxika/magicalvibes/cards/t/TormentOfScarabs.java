package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "78")
@CardRegistration(set = "HA4", collectorNumber = "11")
public class TormentOfScarabs extends Card {

    public TormentOfScarabs() {
        // At the beginning of enchanted player's upkeep, that player loses 3 life unless they
        // sacrifice a nonland permanent of their choice or discard a card.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                UnlessPaysEffect.unlessPays(Subjects.triggeringPlayer(),
                        AnyOfCost.of(new SacrificePermanentCost(new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                "nonland permanent", false), new DiscardCardTypeCost(null, null)),
                        new LoseLifeEffect(3, Subjects.triggeringPlayer())));
    }
}
