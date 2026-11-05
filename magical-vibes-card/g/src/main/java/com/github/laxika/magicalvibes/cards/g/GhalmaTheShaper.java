package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YONE", collectorNumber = "1")
public class GhalmaTheShaper extends Card {

    public GhalmaTheShaper() {
        addTrigger(Triggers.attacks(),
                new ConjureCardNamedIntoHandEffect("SOM", "24", false),
                new CreateTokenEffect(1, "Myr", 1, 1, null,
                        List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
