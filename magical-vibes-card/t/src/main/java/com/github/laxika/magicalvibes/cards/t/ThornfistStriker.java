package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "164")
public class ThornfistStriker extends Card {

    public ThornfistStriker() {
        // Ward {1} (Whenever this creature becomes the target of a spell or ability an opponent
        // controls, counter it unless that player pays {1}.)
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(1));

        // Infusion — Creatures you control get +1/+0 and have trample as long as you gained life this turn.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE), GrantScope.ALL_OWN_CREATURES)));
    }
}
