package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAnyNumberOfTimesPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "124")
public class TaintedAdversary extends Card {

    public TaintedAdversary() {
        CreateTokenEffect decayedZombie = new CreateTokenEffect(
                CardType.CREATURE,
                new Scaled(new XValue(), 2),
                "Zombie", 2, 2, CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE), Set.of(Keyword.DECAYED), Set.of(),
                false, false,
                Map.of(
                        EffectSlot.STATIC, new CantBlockEffect(),
                        EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect()),
                List.of(), false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayManaAnyNumberOfTimesPutCountersOnSelfEffect(
                        "{2}{B}", CounterType.PLUS_ONE_PLUS_ONE, decayedZombie));
    }
}
