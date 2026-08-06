package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SetCombatRequirementThisTurnEffect}: stamps the requested transient combat-requirement
 * flag on the targeted permanent. Each branch sets exactly one flag and logs its own wording; the flags
 * themselves are cleared together at end of turn by {@code Permanent.resetModifiers()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetCombatRequirementThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetCombatRequirementThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetCombatRequirementThisTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        switch (e.requirement()) {
            case MUST_ATTACK -> {
                target.setMustAttackThisTurn(true);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " must attack this turn if able."));
                log.info("Game {} - {} must attack this turn if able", gameData.id, target.getCard().getName());
            }
            case MUST_ATTACK_EFFECT_CONTROLLER -> {
                target.setMustAttackThisTurn(true);
                // Force the creature to attack the ability's controller specifically, not their planeswalkers
                // (Scryfall ruling: "it must attack you, not the planeswalker")
                target.setMustAttackTargetId(entry.getControllerId());

                String controllerName = gameData.playerIdToName.get(entry.getControllerId());

                gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" must attack " + controllerName + " this turn if able.").build());
                log.info("Game {} - {} must attack {} this turn if able", gameData.id, target.getCard().getName(), controllerName);
            }
            case MUST_BLOCK -> {
                target.setMustBlockThisTurnIfAble(true);

                String logEntry = target.getCard().getName() + " blocks this turn if able.";
                gameLogService.append(gameData, GameLog.text(logEntry));

                log.info("Game {} - {} must block this turn if able", gameData.id, target.getCard().getName());
            }
            case MUST_BE_BLOCKED -> {
                target.setMustBeBlockedThisTurn(true);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " must be blocked this turn if able."));
                log.info("Game {} - {} must be blocked this turn if able", gameData.id, target.getCard().getName());
            }
            case MUST_BE_BLOCKED_BY_ALL -> {
                target.setMustBeBlockedByAllThisTurn(true);
                gameLogService.append(gameData, GameLog.textCardText("All creatures able to block ", target.getCard(), " this turn do so."));
                log.info("Game {} - all creatures able to block {} this turn do so", gameData.id, target.getCard().getName());
            }
        }
    }
}
