package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "157")
@CardRegistration(set = "SIR", collectorNumber = "155")
public class FlamebladeAngel extends Card {

    public FlamebladeAngel() {
        addTrigger(Triggers.sourceOpponentControlsDealsDamageToYouOrYourPermanent(),
                new MayEffect(
                        new DealDamageToDamageSourceControllerEffect(1),
                        "Have Flameblade Angel deal 1 damage to that source's controller?"));
    }
}
