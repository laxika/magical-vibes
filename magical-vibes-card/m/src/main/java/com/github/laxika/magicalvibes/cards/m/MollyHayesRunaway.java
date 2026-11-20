package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "698")
public class MollyHayesRunaway extends Card {

    public MollyHayesRunaway() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)
                ),
                "Power-up — {5}{R}: Put two +1/+1 counters on Molly Hayes. Exile the top card of your library. "
                        + "Until the end of your next turn, you may play that card. Activate each power-up ability "
                        + "only once. Reduce the cost by her mana cost if she entered this turn."
        ).withPowerUp());
    }
}
