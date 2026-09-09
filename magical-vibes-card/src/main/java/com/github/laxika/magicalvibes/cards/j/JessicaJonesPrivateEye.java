package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "139")
public class JessicaJonesPrivateEye extends Card {

    public JessicaJonesPrivateEye() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new PutTypedCounterOnSourceCost(CounterType.STUN),
                        new ExileTopCardMayPlayThisTurnEffect(new SourcePower(), false)
                ),
                "{T}, Put a stun counter on Jessica Jones: Exile the top X cards of your library, where X is Jessica Jones's power. You may play those cards this turn."
        ));
    }
}
