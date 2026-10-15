package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "30")
@CardRegistration(set = "TPR", collectorNumber = "19")
public class MountedArchers extends Card {

    public MountedArchers() {
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(1, GrantScope.SELF)),
                "{W}: Mounted Archers can block an additional creature this turn."));
    }
}
