package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "126")
public class BelbesArmor extends Card {

    public BelbesArmor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new BoostTargetCreatureEffect(
                        new Scaled(new XValue(), -1),
                        new XValue())),
                "{X}, {T}: Target creature gets -X/+X until end of turn.",
                TargetFilters.creature()
        ));
    }
}
