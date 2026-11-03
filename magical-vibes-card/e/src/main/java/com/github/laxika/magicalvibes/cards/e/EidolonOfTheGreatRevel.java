package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "94")
@CardRegistration(set = "A25", collectorNumber = "128")
@CardRegistration(set = "SLD", collectorNumber = "422")
@CardRegistration(set = "EA2", collectorNumber = "12")
public class EidolonOfTheGreatRevel extends Card {

    public EidolonOfTheGreatRevel() {
        addTrigger(Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryMaxManaValuePredicate(3)),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));
    }
}
