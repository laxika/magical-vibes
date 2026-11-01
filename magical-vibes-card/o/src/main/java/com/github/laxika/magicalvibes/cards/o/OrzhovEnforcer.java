package com.github.laxika.magicalvibes.cards.o;

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

@CardRegistration(set = "RNA", collectorNumber = "79")
@CardRegistration(set = "RVR", collectorNumber = "87")
public class OrzhovEnforcer extends Card {

    public OrzhovEnforcer() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                1, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
    }
}
