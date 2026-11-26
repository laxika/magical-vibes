package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "89")
@CardRegistration(set = "LEG", collectorNumber = "269")
@CardRegistration(set = "ME3", collectorNumber = "185")
@CardRegistration(set = "SLD", collectorNumber = "1070")
public class VaevictisAsmadi extends Card {

    public VaevictisAsmadi() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{B}{R}{G}", new SacrificeSelfEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(BoostEffect.self(1, 0)),
                "{B}: Vaevictis Asmadi gets +1/+0 until end of turn."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(BoostEffect.self(1, 0)),
                "{R}: Vaevictis Asmadi gets +1/+0 until end of turn."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(BoostEffect.self(1, 0)),
                "{G}: Vaevictis Asmadi gets +1/+0 until end of turn."));
    }
}
