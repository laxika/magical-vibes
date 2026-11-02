package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "160")
@CardRegistration(set = "MM3", collectorNumber = "153")
@CardRegistration(set = "C17", collectorNumber = "165")
public class BroodmateDragon extends Card {

    public BroodmateDragon() {
        // "When this creature enters, create a 4/4 red Dragon creature token with flying."
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Dragon", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()));
    }
}
