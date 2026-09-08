package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "ROE", collectorNumber = "172")
public class WorldAtWar extends Card {

    public WorldAtWar() {
        addEffect(EffectSlot.SPELL, new AdditionalCombatMainPhaseEffect(
                1, new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)));
    }
}
