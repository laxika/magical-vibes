package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

@CardRegistration(set = "ICE", collectorNumber = "101")
public class Snowfall extends Card {

    public Snowfall() {
        // Cumulative upkeep {U}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{U}"));

        // Whenever an Island is tapped for mana, its controller may add an additional {U}.
        // If that Island is snow, its controller may add an additional {U}{U} instead.
        // Spend this mana only to pay cumulative upkeep costs.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect(
                        CardSubtype.ISLAND,
                        ManaColor.BLUE,
                        1,
                        2,
                        new ManaRestriction.CumulativeUpkeepCosts()));
    }
}
