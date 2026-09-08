package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "89")
public class VaevictisAsmadi extends Card {

    public VaevictisAsmadi() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{B}{R}{G}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new BoostSelfEffect(1, 0)),
                "{B}: Vaevictis Asmadi gets +1/+0 until end of turn."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: Vaevictis Asmadi gets +1/+0 until end of turn."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new BoostSelfEffect(1, 0)),
                "{G}: Vaevictis Asmadi gets +1/+0 until end of turn."));
    }
}
