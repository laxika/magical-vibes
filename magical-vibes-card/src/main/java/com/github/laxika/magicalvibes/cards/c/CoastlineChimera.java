package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "44")
public class CoastlineChimera extends Card {

    public CoastlineChimera() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(1, GrantScope.SELF)),
                "{1}{W}: Coastline Chimera can block an additional creature this turn."));
    }
}
