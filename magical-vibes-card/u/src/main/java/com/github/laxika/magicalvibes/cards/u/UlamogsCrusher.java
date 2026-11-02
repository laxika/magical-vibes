package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "13")
@CardRegistration(set = "MM2", collectorNumber = "7")
@CardRegistration(set = "DDP", collectorNumber = "44")
@CardRegistration(set = "UMA", collectorNumber = "8")
@CardRegistration(set = "CMM", collectorNumber = "6")
@CardRegistration(set = "M3C", collectorNumber = "161")
public class UlamogsCrusher extends Card {

    public UlamogsCrusher() {
        addStaticEffect(new MustAttackEffect());
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                2, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
