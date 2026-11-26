package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "88")
@CardRegistration(set = "AA4", collectorNumber = "17")
public class DragonTyrant extends Card {

    public DragonTyrant() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{R}{R}{R}{R}", new SacrificeSelfEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(BoostEffect.self(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}
