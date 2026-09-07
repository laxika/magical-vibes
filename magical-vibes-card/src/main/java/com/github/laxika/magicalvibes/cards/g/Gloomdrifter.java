package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "61")
public class Gloomdrifter extends Card {

    public Gloomdrifter() {
        // Threshold — as long as there are seven or more cards in your graveyard,
        // this creature has "When this creature enters, nonblack creatures get -2/-2 until end of turn."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))))));
    }
}
