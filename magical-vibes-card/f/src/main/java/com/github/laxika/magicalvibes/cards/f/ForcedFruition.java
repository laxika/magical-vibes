package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "66")
@CardRegistration(set = "SLD", collectorNumber = "1177")
@CardRegistration(set = "WOT", collectorNumber = "18")
public class ForcedFruition extends Card {

    public ForcedFruition() {
        // Whenever an opponent casts a spell, that player draws seven cards.
        addTrigger(Triggers.opponentCastsSpell().anySpell(), new DrawCardForTargetPlayerEffect(7));
    }
}
