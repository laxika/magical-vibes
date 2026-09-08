package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "105")
public class AnuridSwarmsnapper extends Card {

    public AnuridSwarmsnapper() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(1, GrantScope.SELF)),
                "{1}{G}: This creature can block an additional creature this turn."
        ));
    }
}
