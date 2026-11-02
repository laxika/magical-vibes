package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CaetusSeaTyrantOfSegovia;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "63")
public class InvasionOfSegovia extends Card {

    public InvasionOfSegovia() {
        setBackFaceCard(new CaetusSeaTyrantOfSegovia());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                2, "Kraken", 1, 1, CardColor.BLUE, List.of(CardSubtype.KRAKEN),
                Set.of(Keyword.TRAMPLE), Set.of(), List.of()));
    }

    @Override
    public String getBackFaceClassName() {
        return "CaetusSeaTyrantOfSegovia";
    }
}
