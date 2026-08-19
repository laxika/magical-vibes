package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "2")
public class AuroraGriffin extends Card {

    public AuroraGriffin() {
        addActivatedAbility(new ActivatedAbility(
                false, "{W}",
                List.of(new GrantColorUntilEndOfTurnEffect(CardColor.WHITE)),
                "{W}: Target permanent becomes white until end of turn."
        ));
    }
}
