package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IfWonClashEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "LRW", collectorNumber = "13")
public class EntanglingTrap extends Card {

    public EntanglingTrap() {
        // Whenever you clash, tap target creature an opponent controls. If you won, that creature
        // doesn't untap during its controller's next untap step. (Fired after the clash ends by
        // TriggerCollectionService.performClash; the target is chosen via the ClashTriggerTarget
        // interaction. The IfWonClashEffect clause is applied only on a won clash.)
        addEffect(EffectSlot.ON_CONTROLLER_CLASHES, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_CONTROLLER_CLASHES,
                new IfWonClashEffect(new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
