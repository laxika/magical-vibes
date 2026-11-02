package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPCA", collectorNumber = "79")
public class TemberCity extends Card {

    public TemberCity() {
        addTrigger(Triggers.playerTapsLandForMana(), new DealDamageOnLandTapEffect(1));
        addTrigger(Triggers.chaosEnsues(), new SacrificePermanentsEffect(
                1, new PermanentNotPredicate(new PermanentIsLandPredicate()), SacrificeRecipient.EACH_OPPONENT));
    }
}
