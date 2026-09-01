package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceAtEndOfCombatEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "8")
public class KjeldoranHomeGuard extends Card {

    private static final CreateTokenEffect DESERTER_TOKEN = new CreateTokenEffect(
            "Deserter", 0, 1, CardColor.WHITE, List.of(CardSubtype.DESERTER), Set.of(), Set.<CardType>of());

    public KjeldoranHomeGuard() {
        // At end of combat, if this creature attacked or blocked this combat, put a -0/-1 counter on
        // this creature and create a 0/1 white Deserter creature token. Scheduled from the attack /
        // block trigger so the counter lands after combat damage, leaving it at full toughness.
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnSourceAtEndOfCombatEffect(
                CounterType.MINUS_ZERO_MINUS_ONE, 1, DESERTER_TOKEN));
        addEffect(EffectSlot.ON_BLOCK, new PutCounterOnSourceAtEndOfCombatEffect(
                CounterType.MINUS_ZERO_MINUS_ONE, 1, DESERTER_TOKEN), TriggerMode.ONCE_PER_BLOCK);
    }
}
