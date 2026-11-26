package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "106")
@CardRegistration(set = "DRK", collectorNumber = "36")
@CardRegistration(set = "ME1", collectorNumber = "51")
public class SunkenCity extends Card {

    public SunkenCity() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {U}{U}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{U}{U}", new SacrificeSelfEffect()));
        // Blue creatures get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.allCreatures(),
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
    }
}
