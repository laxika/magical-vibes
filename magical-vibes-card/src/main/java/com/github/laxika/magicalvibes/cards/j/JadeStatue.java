package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "9ED", collectorNumber = "300")
public class JadeStatue extends Card {

    public JadeStatue() {
        // {2}: Jade Statue becomes a 3/6 Golem artifact creature until end of combat. Activate only during combat.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AnimatePermanentsEffect(3, 6, List.of(CardSubtype.GOLEM), Set.of(), null,
                        Set.of(), GrantScope.SELF, EffectDuration.UNTIL_END_OF_COMBAT)),
                "{2}: Jade Statue becomes a 3/6 Golem artifact creature until end of combat. Activate only during combat.",
                ActivationTimingRestriction.ONLY_DURING_COMBAT
        ));
    }
}
