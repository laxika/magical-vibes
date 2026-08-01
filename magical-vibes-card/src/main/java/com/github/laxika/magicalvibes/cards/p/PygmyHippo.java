package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrainDefendingPlayerLandManaDelayedColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "VIS", collectorNumber = "133")
public class PygmyHippo extends Card {

    public PygmyHippo() {
        // Whenever this creature attacks and isn't blocked, you may have defending player activate
        // a mana ability of each land they control and lose all unspent mana. If you do, this
        // creature assigns no combat damage this turn and at the beginning of your next main phase
        // this turn, you add an amount of {C} equal to the amount of mana that player lost this way.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(SequenceEffect.of(
                        new DrainDefendingPlayerLandManaDelayedColorlessEffect(),
                        new AssignNoCombatDamageEffect()),
                        "You may have defending player activate a mana ability of each land they control and lose all unspent mana. If you do, this creature assigns no combat damage this turn and you add that much {C} at your next main phase this turn."));
    }
}
