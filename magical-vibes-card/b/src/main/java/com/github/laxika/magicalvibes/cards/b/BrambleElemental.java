package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "154")
@CardRegistration(set = "PC2", collectorNumber = "59")
@CardRegistration(set = "PCA", collectorNumber = "59")
public class BrambleElemental extends Card {

    public BrambleElemental() {
        addTrigger(Triggers.auraBecomesAttachedToThis(), new CreateTokenEffect(
                2,
                "Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        ));
    }
}
