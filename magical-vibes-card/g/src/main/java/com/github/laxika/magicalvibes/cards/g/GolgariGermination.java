package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "209")
@CardRegistration(set = "MM3", collectorNumber = "167")
@CardRegistration(set = "DDJ", collectorNumber = "70")
public class GolgariGermination extends Card {

    public GolgariGermination() {
        // Whenever a nontoken creature you control dies, create a 1/1 green Saproling creature token.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }
}
