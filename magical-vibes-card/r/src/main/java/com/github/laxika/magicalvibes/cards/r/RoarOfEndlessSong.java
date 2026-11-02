package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "220")
public class RoarOfEndlessSong extends Card {

    public RoarOfEndlessSong() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                "Elephant", 5, 5, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                "Elephant", 5, 5, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
        addTrigger(Triggers.chapter(3), new DoubleAllOwnCreaturesPowerToughnessEffect());
    }
}
