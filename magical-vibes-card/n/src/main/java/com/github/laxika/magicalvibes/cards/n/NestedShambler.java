package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "95")
@CardRegistration(set = "BLC", collectorNumber = "185")
public class NestedShambler extends Card {

    public NestedShambler() {
        // When this creature dies, create X tapped 1/1 green Squirrel creature tokens, where X is this creature's power.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                new SourcePower(), "Squirrel", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()).withTapped(true));
    }
}
