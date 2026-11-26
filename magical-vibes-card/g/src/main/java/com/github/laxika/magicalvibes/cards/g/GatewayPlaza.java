package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "GRN", collectorNumber = "247")
@CardRegistration(set = "RNA", collectorNumber = "247")
@CardRegistration(set = "WAR", collectorNumber = "246")
@CardRegistration(set = "PIO", collectorNumber = "262")
public class GatewayPlaza extends Card {

    public GatewayPlaza() {
        addStaticEffect(new EntersTappedEffect());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.unlessYouPay("{1}", new SacrificeSelfEffect()));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
