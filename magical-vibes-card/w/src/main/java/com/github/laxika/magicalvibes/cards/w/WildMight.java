package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "134")
public class WildMight extends Card {

    public WildMight() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(new MayPayManaEffect(
                        "{2}",
                        null,
                        "Pay {2} to prevent the additional +4/+4?",
                        MayPayPayer.ANY_PLAYER,
                        new BoostTargetCreatureEffect(4, 4),
                        0));
    }
}
