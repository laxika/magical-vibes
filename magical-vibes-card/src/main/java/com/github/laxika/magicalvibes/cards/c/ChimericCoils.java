package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "108")
public class ChimericCoils extends Card {

    public ChimericCoils() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{1}",
                List.of(
                        new AnimatePermanentsEffect(new XValue(), new XValue(),
                                List.of(CardSubtype.CONSTRUCT), Set.of(), null, Set.of(),
                                GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null),
                        new SacrificeSelfAtEndStepEffect()),
                "{X}{1}: This artifact becomes an X/X Construct artifact creature. Sacrifice it at the beginning of the next end step."
        ));
    }
}
