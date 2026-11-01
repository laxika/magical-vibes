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

@CardRegistration(set = "THB", collectorNumber = "88")
public class DiscordantPiper extends Card {

    public DiscordantPiper() {
        // When this creature dies, create a 0/1 white Goat creature token.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                "Goat", 0, 1, CardColor.WHITE, List.of(CardSubtype.GOAT), Set.of(), Set.of()));
    }
}
