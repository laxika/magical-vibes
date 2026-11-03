package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "68")
@CardRegistration(set = "WOT", collectorNumber = "45")
public class LeylineOfLightning extends Card {

    public LeylineOfLightning() {
        addStaticEffect(new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Lightning on the battlefield?"
        ));
        addTrigger(Triggers.youCastSpell().anySpell(),
                new MayPayManaEffect("{1}", new DealDamageToTargetPlayerOrPlaneswalkerEffect(1), "Pay {1} to have Leyline of Lightning deal 1 damage to target player or planeswalker?"));
    }
}
