package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "5")
@CardRegistration(set = "RVR", collectorNumber = "7")
public class BasilicaGuards extends Card {

    public BasilicaGuards() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                new MayPayManaEffect("{W/B}",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true),
                        "Pay {W/B} to extort?"));
    }
}
