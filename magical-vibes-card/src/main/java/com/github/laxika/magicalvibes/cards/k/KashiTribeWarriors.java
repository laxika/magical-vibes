package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "CHK", collectorNumber = "221")
public class KashiTribeWarriors extends Card {

    public KashiTribeWarriors() {
        // Whenever this creature deals combat damage to a creature, tap that creature and it doesn't
        // untap during its controller's next untap step. The damaged creature is baked as targetId by
        // ON_COMBAT_DAMAGE_TO_CREATURE (non-targeting), so both halves act on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
