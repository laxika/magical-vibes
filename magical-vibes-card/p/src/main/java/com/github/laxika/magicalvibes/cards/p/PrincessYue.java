package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAsNamedLandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "95")
public class PrincessYue extends Card {

    public PrincessYue() {
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, Triggers.NONLAND)),
                        ObjectRelation.THIS),
                new ReturnDyingCreatureToBattlefieldAsNamedLandEffect("Moon"));
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new ScryEffect(2)), "{T}: Scry 2."));
    }
}
