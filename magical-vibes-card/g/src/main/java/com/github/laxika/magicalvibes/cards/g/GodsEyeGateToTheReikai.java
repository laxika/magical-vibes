package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "164")
public class GodsEyeGateToTheReikai extends Card {

    public GodsEyeGateToTheReikai() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new CreateTokenEffect("Spirit", 1, 1, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
    }
}
