package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "105")
public class UnderworldHermit extends Card {

    public UnderworldHermit() {
        // When this creature enters, create Squirrels equal to your black devotion.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK),
                "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL),
                Set.of(), Set.of()));
    }
}
