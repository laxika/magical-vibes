package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "167")
@CardRegistration(set = "7ED", collectorNumber = "178")
@CardRegistration(set = "6ED", collectorNumber = "172")
public class CrimsonHellkite extends Card {

    public CrimsonHellkite() {
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new DealDamageToTargetCreatureEffect(new XValue())),
                "{X}, {T}: This creature deals X damage to target creature. Spend only red mana on X.")
                .withXColorRestriction(ManaColor.RED));
    }
}
