package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "106")
@CardRegistration(set = "VMA", collectorNumber = "190")
@CardRegistration(set = "DDK", collectorNumber = "68")
@CardRegistration(set = "EMA", collectorNumber = "150")
@CardRegistration(set = "SLD", collectorNumber = "2210")
@CardRegistration(set = "DMR", collectorNumber = "144")
public class SulfuricVortex extends Card {

    public SulfuricVortex() {
        addStaticEffect(new PlayersCantGainLifeEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(2, DamageRecipient.ACTIVE_PLAYER));
    }
}
