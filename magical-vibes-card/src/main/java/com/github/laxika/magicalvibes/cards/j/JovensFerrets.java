package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceWasBlockedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentAtEndOfCombatEffect;

@CardRegistration(set = "HML", collectorNumber = "89")
public class JovensFerrets extends Card {

    public JovensFerrets() {
        // Whenever this creature attacks, it gets +0/+2 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(0, 2));

        // At end of combat, tap all creatures that blocked this creature this turn. They don't untap
        // during their controller's next untap step.
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new SourceWasBlockedThisTurn(),
                        new TapCombatOpponentAtEndOfCombatEffect()));
    }
}
