package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentAtEndOfCombatEffect;

@CardRegistration(set = "HML", collectorNumber = "89")
public class JovensFerrets extends Card {

    public JovensFerrets() {
        // Whenever this creature attacks, it gets +0/+2 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(0, 2));

        // At end of combat, tap all creatures that blocked this creature this turn. They don't untap
        // during their controller's next untap step. Scheduled per blocker as it becomes blocked, so
        // every blocker this turn is covered; the tap itself happens at end of combat.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new TapCombatOpponentAtEndOfCombatEffect(),
                TriggerMode.PER_BLOCKER);
    }
}
