package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "76")
public class GorillaTactics extends Card {

    public GorillaTactics() {
        CreateTokenEffect gorilla = new CreateTokenEffect(
                "Gorilla", 2, 2, CardColor.GREEN, List.of(CardSubtype.GORILLA), Set.of(), Set.of());
        addSpellEffect(gorilla);
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), gorilla.withAmount(2));
    }
}
