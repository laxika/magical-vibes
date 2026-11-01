package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "197")
public class DogWalker extends Card {

    public DogWalker() {
        addMorph("{R/W}{R/W}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new CreateTokenEffect(2, "Dog", 1, 1, CardColor.WHITE, List.of(CardSubtype.DOG), Set.of(), Set.of(), true));
    }
}
