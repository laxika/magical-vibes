package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.IfWonClashEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "13")
public class EntanglingTrap extends Card {

    public EntanglingTrap() {
        // Whenever you clash, tap target creature an opponent controls. If you won, that creature
        // doesn't untap during its controller's next untap step. (Fired after the clash ends by
        // TriggerCollectionService.performClash; the target is chosen via the ClashTriggerTarget
        // interaction. The IfWonClashEffect clause is applied only on a won clash.)
        addTrigger(Triggers.youClash(), new TapPermanentsEffect(TapUntapScope.TARGET),
                new IfWonClashEffect(new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
