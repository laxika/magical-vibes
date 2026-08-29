package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;

@CardRegistration(set = "DFT", collectorNumber = "112")
public class AdrenalineJockey extends Card {

    public AdrenalineJockey() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                SpellCastDamageToCasterEffect.whenCasterIsNotActiveTurn(4));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
