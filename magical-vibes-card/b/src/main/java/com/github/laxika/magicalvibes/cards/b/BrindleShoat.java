package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VMA", collectorNumber = "199")
@CardRegistration(set = "PC2", collectorNumber = "60")
@CardRegistration(set = "PCA", collectorNumber = "60")
@CardRegistration(set = "2X2", collectorNumber = "139")
public class BrindleShoat extends Card {

    public BrindleShoat() {
        // When this creature dies, create a 3/3 green Boar creature token.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                "Boar", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.BOAR), Set.of(), Set.of()
        ));
    }
}
