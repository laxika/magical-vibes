package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "403")
@CardRegistration(set = "LEA", collectorNumber = "273")
@CardRegistration(set = "4ED", collectorNumber = "353")
@CardRegistration(set = "2ED", collectorNumber = "274")
@CardRegistration(set = "8ED", collectorNumber = "317")
@CardRegistration(set = "6ED", collectorNumber = "316")
@CardRegistration(set = "7ED", collectorNumber = "322")
@CardRegistration(set = "SUM", collectorNumber = "279")
@CardRegistration(set = "3ED", collectorNumber = "279")
@CardRegistration(set = "LEB", collectorNumber = "274")
public class ThroneOfBone extends Card {

    public ThroneOfBone() {
        // Whenever a player casts a black spell, you may pay {1}. If you do, you gain 1 life.
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?"));
    }
}
