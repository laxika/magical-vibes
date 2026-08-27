package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedArtifactThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MKM", collectorNumber = "59")
public class FurtiveCourier extends Card {

    public FurtiveCourier() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerSacrificedArtifactThisTurn(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect());
        addEffect(EffectSlot.ON_ATTACK, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
