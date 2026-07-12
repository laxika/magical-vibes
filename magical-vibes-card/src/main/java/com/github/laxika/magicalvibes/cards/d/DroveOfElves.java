package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "112")
public class DroveOfElves extends Card {

    public DroveOfElves() {
        // Hexproof is auto-loaded from Scryfall metadata.
        // Power and toughness are each equal to the number of green permanents you control.
        PermanentCount greenPermanents = new PermanentCount(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN)), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(greenPermanents, greenPermanents));
    }
}
