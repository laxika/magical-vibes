package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YEOE", collectorNumber = "26")
public class ValMaroonedSurveyor extends Card {

    public ValMaroonedSurveyor() {
        CardEffect trigger = SequenceEffect.of(
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(2));

        addTrigger(Triggers.youDiscover(), trigger);
        addTrigger(Triggers.youInvestigate(), trigger);
        addTrigger(Triggers.youScry(), trigger);
        addTrigger(Triggers.youSurveil(), trigger);
        addTrigger(Triggers.youSeek(), trigger);
    }
}
