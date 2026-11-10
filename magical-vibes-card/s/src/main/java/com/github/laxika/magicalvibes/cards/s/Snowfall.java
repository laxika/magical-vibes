package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddRestrictedManaWhenLandTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "101")
public class Snowfall extends Card {

    public Snowfall() {
        // Cumulative upkeep {U}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{U}"));

        // Whenever an Island is tapped for mana, its controller may add an additional {U}.
        // If that Island is snow, its controller may add an additional {U}{U} instead.
        // Spend this mana only to pay cumulative upkeep costs.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new AddRestrictedManaWhenLandTappedForManaEffect(ManaColor.BLUE,
                        1,
                        2,
                        new ManaRestriction.CumulativeUpkeepCosts()));
    }
}
