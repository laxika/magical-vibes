package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "19")
public class CetaDisciple extends Card {

    public CetaDisciple() {
        addActivatedAbility(new ActivatedAbility(
                true, "{R}",
                List.of(new BoostTargetCreatureEffect(2, 0)),
                "{R}, {T}: Target creature gets +2/+0 until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true, "{G}",
                List.of(new AwardAnyColorManaEffect()),
                "{G}, {T}: Add one mana of any color."
        ));
    }
}
