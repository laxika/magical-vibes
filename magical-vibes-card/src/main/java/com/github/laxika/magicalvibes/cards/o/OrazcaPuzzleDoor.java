package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "68")
public class OrazcaPuzzleDoor extends Card {

    public OrazcaPuzzleDoor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        LookAtTopCardsEffect.chooseNToHandRestToGraveyard(2, 1)
                ),
                "{1}, {T}, Sacrifice this artifact: Look at the top two cards of your library. Put one of those cards into your hand and the other into your graveyard."
        ));
    }
}
