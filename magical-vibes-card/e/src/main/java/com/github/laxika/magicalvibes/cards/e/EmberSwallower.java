package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "120")
public class EmberSwallower extends Card {

    public EmberSwallower() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}{R}",
                List.of(new MonstrosityEffect(3)),
                "{5}{R}{R}: Monstrosity 3."
        ));

        addTrigger(Triggers.becomesMonstrous(), new SacrificePermanentsEffect(
                3, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
