package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedArtifactThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "59")
public class FurtiveCourier extends Card {

    public FurtiveCourier() {
        addStaticEffect(new ConditionalEffect(
                new ControllerSacrificedArtifactThisTurn(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addTrigger(Triggers.attacks(), new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
