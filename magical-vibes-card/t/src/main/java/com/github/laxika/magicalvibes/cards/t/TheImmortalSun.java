package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerLoyaltyAbilitiesCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "180")
@CardRegistration(set = "CMM", collectorNumber = "393")
@CardRegistration(set = "CMM", collectorNumber = "608")
public class TheImmortalSun extends Card {

    public TheImmortalSun() {
        addStaticEffect(new PlaneswalkerLoyaltyAbilitiesCantBeActivatedEffect());
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DrawCardEffect(1));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 1, CostModificationScope.SELF));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
    }
}
