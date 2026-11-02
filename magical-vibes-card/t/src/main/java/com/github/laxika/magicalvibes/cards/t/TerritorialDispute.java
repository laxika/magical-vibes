package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayLandsIfPermanentCountEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "217")
public class TerritorialDispute extends Card {

    public TerritorialDispute() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you sacrifice a land.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new SacrificePermanentCost(new PermanentIsLandPredicate(), "a land"),
                List.of(new SacrificeSelfEffect()),
                true));
        // Players can't play lands.
        addStaticEffect(new PlayersCantPlayLandsIfPermanentCountEffect(0, new PermanentTruePredicate()));
    }
}
