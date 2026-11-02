package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "6")
@CardRegistration(set = "C21", collectorNumber = "82")
@CardRegistration(set = "C19", collectorNumber = "60")
public class DesolationTwin extends Card {

    public DesolationTwin() {
        addTrigger(Triggers.youCastThisSpell(), new CreateTokenEffect(
                "Eldrazi", 10, 10, null, List.of(CardSubtype.ELDRAZI), Set.of(), Set.of()));
    }
}
