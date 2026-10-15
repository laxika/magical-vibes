package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "105")
public class HungeringYeti extends Card {

    public HungeringYeti() {
        setFlashCastCondition(new ControlsPermanent(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.BLUE))));
    }
}
