package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "62")
public class RelicRunner extends Card {

    public RelicRunner() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect());
    }
}
