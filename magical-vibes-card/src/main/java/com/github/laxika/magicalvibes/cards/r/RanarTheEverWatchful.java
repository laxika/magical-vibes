package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceFirstForetellCostEachTurnEffect;

@CardRegistration(set = "KHC", collectorNumber = "2")
public class RanarTheEverWatchful extends Card {

    public RanarTheEverWatchful() {
        addEffect(EffectSlot.STATIC, new ReduceFirstForetellCostEachTurnEffect(2));
        CreateTokenEffect spirit = CreateTokenEffect.whiteSpirit(1);
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_EXILED_FROM_HAND, spirit);
        addEffect(EffectSlot.ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT, spirit);
    }
}
