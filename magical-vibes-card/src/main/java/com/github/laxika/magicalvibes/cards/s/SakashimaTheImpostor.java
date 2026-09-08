package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "53")
public class SakashimaTheImpostor extends Card {

    public SakashimaTheImpostor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", "Sakashima the Impostor",
                Set.of(CardSupertype.LEGENDARY),
                List.of(new ActivatedAbility(
                        false,
                        "{2}{U}{U}",
                        List.of(new ReturnSelfToHandAtEndStepEffect()),
                        "{2}{U}{U}: Return Sakashima the Impostor to its owner's hand at the beginning of the next end step."
                ))
        ));
    }
}
