package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Queues a controller mill rider on a mana ability ("Add {C}. When you do, mill two cards") as a
 * reflexive triggered ability. A triggered ability that can't add mana is not a mana ability, so
 * the mill waits for the stack (CR 605.5a) instead of resolving with the mana.
 */
@Component
public class MillControllerManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        MillEffect mill = (MillEffect) effect;
        if (mill.recipient() != MillRecipient.CONTROLLER) {
            return;
        }

        gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                permanent.getCard(), playerId, permanent.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(mill)), null, permanent.getId()));
    }
}
