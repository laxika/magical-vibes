package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "158")
public class SummoningStation extends Card {

    public SummoningStation() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect("Pincher", 2, 2, null,
                        List.of(CardSubtype.PINCHER), Set.of(), Set.of())),
                "{T}: Create a 2/2 colorless Pincher creature token."
        ));

        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                        "Untap Summoning Station?"));
    }
}
