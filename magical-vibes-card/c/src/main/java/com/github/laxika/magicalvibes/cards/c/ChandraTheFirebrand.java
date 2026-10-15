package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "124")
@CardRegistration(set = "M13", collectorNumber = "123")
public class ChandraTheFirebrand extends Card {

    public ChandraTheFirebrand() {
        // +1: Chandra, the Firebrand deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "+1: Chandra, the Firebrand deals 1 damage to any target."
        ));

        // −2: When you next cast an instant or sorcery spell this turn, copy that spell.
        // You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CopyNextInstantOrSorceryCastThisTurnEffect()),
                "−2: When you next cast an instant or sorcery spell this turn, copy that spell. "
                        + "You may choose new targets for the copy."
        ));

        // −6: Chandra, the Firebrand deals 6 damage to each of up to six targets.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new DealDamageToEachTargetEffect(new Fixed(6))),
                "−6: Chandra, the Firebrand deals 6 damage to each of up to six targets.",
                null, -6, null, null,
                List.of(), 0, 6
        ));
    }
}
