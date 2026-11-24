package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "259")
public class PlanetariumOfWanShiTong extends Card {

    public PlanetariumOfWanShiTong() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ScryEffect(2)),
                "{1}, {T}: Scry 2."));

        addTrigger(List.of(Triggers.youScry(), Triggers.youSurveil()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                LookAtTopCardsAndMayCastEffect.lookAt(1).leavingTheRest());
    }
}
