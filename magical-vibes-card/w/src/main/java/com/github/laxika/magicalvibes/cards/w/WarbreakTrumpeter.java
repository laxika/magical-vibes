package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "116")
public class WarbreakTrumpeter extends Card {

    public WarbreakTrumpeter() {
        addMorph("{X}{X}{R}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new CreateTokenEffect(
                new XValue(), "Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
