package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureWithLeastPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

/**
 * Drop of Honey — {G} Enchantment.
 * Destroys a creature with the least power during its controller's upkeep and sacrifices itself
 * when there are no creatures on the battlefield.
 */
@CardRegistration(set = "ME4", collectorNumber = "150")
@CardRegistration(set = "ARN", collectorNumber = "47")
public class DropOfHoney extends Card {

    public DropOfHoney() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DestroyCreatureWithLeastPowerEffect(true));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.playerBattlefields.values().stream()
                        .flatMap(List::stream)
                        .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE)),
                List.of(new SacrificeSelfEffect()),
                "Drop of Honey's state-triggered ability"
        ));
    }
}
