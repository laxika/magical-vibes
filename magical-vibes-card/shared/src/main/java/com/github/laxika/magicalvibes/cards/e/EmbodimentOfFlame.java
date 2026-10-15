package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

/** Back face of Flame Channeler. */
public class EmbodimentOfFlame extends Card {

    public EmbodimentOfFlame() {
        addEffect(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.FLAME));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.FLAME),
                        new ExileTopCardMayPlayThisTurnEffect(false)
                ),
                "{1}, Remove a flame counter from this creature: Exile the top card of your library. "
                        + "You may play that card this turn."
        ));
    }
}
