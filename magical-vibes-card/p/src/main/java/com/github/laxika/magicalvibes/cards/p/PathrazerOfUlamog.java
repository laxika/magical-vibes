package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "9")
@CardRegistration(set = "CMM", collectorNumber = "4")
public class PathrazerOfUlamog extends Card {

    public PathrazerOfUlamog() {
        addStaticEffect(new CantBeBlockedByFewerThanNCreaturesEffect(3));
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                3, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
