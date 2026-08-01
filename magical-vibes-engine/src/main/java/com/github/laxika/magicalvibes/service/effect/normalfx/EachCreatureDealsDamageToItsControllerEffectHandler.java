package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsDamageToItsControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachCreatureDealsDamageToItsControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachCreatureDealsDamageToItsControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachCreatureDealsDamageToItsControllerEffect) effect;
        if (e.damage() <= 0) {
            return;
        }

        // Snapshot creatures first — damage does not remove them, but controllers / prevention
        // state can change mid-loop via replacements and triggers.
        List<Permanent> creatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatures.add(permanent);
                }
            }
        }

        for (Permanent creature : creatures) {
            // Re-check presence — a prior damage event's replacement could have bounced/exiled it.
            if (gameQueryService.findPermanentById(gameData, creature.getId()) == null) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
            if (controllerId == null) {
                continue;
            }

            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, creature)) {
                gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s damage is prevented."));
                continue;
            }

            // The creature is the damage source (not the resolving spell).
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    creature.getCard(),
                    controllerId,
                    creature.getCard().getName() + "'s ability",
                    List.of(),
                    null,
                    creature.getId());

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), damageEntry);
            damageSupport.dealDamageToPlayer(gameData, damageEntry, controllerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
