package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "145")
public class CogworkAssembler extends Card {

    public CogworkAssembler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new CreateTokenCopyOfTargetPermanentEffect(true, true)),
                "{7}: Create a token that's a copy of target artifact. That token gains haste. "
                        + "Exile it at the beginning of the next end step.",
                TargetFilters.artifact()
        ));
    }
}
