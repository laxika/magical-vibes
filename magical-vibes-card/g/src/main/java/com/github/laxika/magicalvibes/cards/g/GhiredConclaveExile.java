package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "42")
public class GhiredConclaveExile extends Card {

    public GhiredConclaveExile() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect("Rhino", 4, 4,
                CardColor.GREEN, List.of(CardSubtype.RHINO), Set.of(Keyword.TRAMPLE), Set.of()));
        addTrigger(Triggers.attacks(), new PopulateEffect(), new MakeCreatedPermanentsAttackingEffect(true));
    }
}
