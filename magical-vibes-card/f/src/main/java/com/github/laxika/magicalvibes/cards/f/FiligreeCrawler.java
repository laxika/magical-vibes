package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "150")
public class FiligreeCrawler extends Card {

    public FiligreeCrawler() {
        // When this creature dies, create a 1/1 colorless Thopter artifact creature token with flying.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                "Thopter",
                1,
                1,
                null,
                List.of(CardSubtype.THOPTER),
                Set.of(Keyword.FLYING),
                Set.of(CardType.ARTIFACT)
        ));
    }
}
