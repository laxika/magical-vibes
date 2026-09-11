package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "177")
public class SeaGateWreckage extends Card {

    public SeaGateWreckage() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{C}",
                List.of(new DrawCardEffect(1)),
                "{2}{C}, {T}: Draw a card. Activate only if you have no cards in hand."
        ).withActivationCondition(new ControllerHandEmpty(),
                "Activate only if you have no cards in hand"));
    }
}
