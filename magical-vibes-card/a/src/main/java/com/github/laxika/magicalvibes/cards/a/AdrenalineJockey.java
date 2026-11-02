package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "112")
public class AdrenalineJockey extends Card {

    public AdrenalineJockey() {
        addTrigger(Triggers.playerCastsSpell(),
                SpellCastDamageToCasterEffect.whenCasterIsNotActiveTurn(4));
        addTrigger(Triggers.youActivateExhaustAbility(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
