package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "283")
@CardRegistration(set = "VIS", collectorNumber = "144")
public class DragonMask extends Card {

    public DragonMask() {
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        new BoostTargetCreatureEffect(2, 2),
                        new ReturnTargetPermanentToHandAtEndStepEffect()
                ),
                "{3}, {T}: Target creature you control gets +2/+2 until end of turn. "
                        + "Return it to its owner's hand at the beginning of the next end step.",
                TargetFilters.creatureYouControl()
        ));
    }
}
