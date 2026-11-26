package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "CHR", collectorNumber = "34")
@CardRegistration(set = "ME4", collectorNumber = "86")
@CardRegistration(set = "ARN", collectorNumber = "27")
public class HasranOgress extends Card {

    public HasranOgress() {
        addTrigger(Triggers.attacks(), UnlessPaysEffect.unlessYouPay("{2}", new DealDamageToPlayersEffect(3, Subjects.you())));
    }
}
