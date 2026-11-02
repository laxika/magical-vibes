package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "231")
@CardRegistration(set = "SLD", collectorNumber = "2238")
@CardRegistration(set = "M3C", collectorNumber = "279")
public class ZiatoraTheIncinerator extends Card {

    public ZiatoraTheIncinerator() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(
                                CreateTokenEffect.ofTreasureToken(3)),
                        "Sacrifice another creature?"));
    }
}
