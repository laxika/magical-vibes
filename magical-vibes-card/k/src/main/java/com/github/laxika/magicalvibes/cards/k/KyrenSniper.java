package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "199")
public class KyrenSniper extends Card {

    public KyrenSniper() {
        // At the beginning of your upkeep, you may have this creature deal 1 damage to target
        // player or planeswalker.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                "Deal 1 damage to target player or planeswalker?"));
    }
}
