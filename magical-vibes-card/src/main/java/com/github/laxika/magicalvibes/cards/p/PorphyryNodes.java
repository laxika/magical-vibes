package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureWithLeastPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

/**
 * Porphyry Nodes — {W} Enchantment.
 * Destroys a creature with the least power during its controller's upkeep and sacrifices itself
 * when there are no creatures on the battlefield.
 */
@CardRegistration(set = "PLC", collectorNumber = "28")
public class PorphyryNodes extends Card {

    public PorphyryNodes() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DestroyCreatureWithLeastPowerEffect(true));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.playerBattlefields.values().stream()
                        .flatMap(List::stream)
                        .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE)),
                List.of(new SacrificeSelfEffect()),
                "Porphyry Nodes's state-triggered ability"
        ));
    }
}
