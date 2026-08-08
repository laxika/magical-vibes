package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
        if (e.scope() == GrantScope.ALL_OWN_CREATURES) {
            resolveForOwnCreatures(gameData, entry, e.requirement());
            return;
        }

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
            case MUST_ATTACK_OR_BLOCK -> {
                // A creature can only attack on its controller's turn and only block on someone
                // else's, so "attacks or blocks this combat" leaves its controller no real choice —
                // resolve the disjunction to whichever half is available this combat.
                boolean controllerIsActivePlayer = gameData.activePlayerId != null
                        && gameData.activePlayerId.equals(gameQueryService.findPermanentController(gameData, target.getId()));
                if (controllerIsActivePlayer) {
                    target.setMustAttackThisTurn(true);
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " attacks this combat if able."));
                } else {
                    target.setMustBlockThisTurnIfAble(true);
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " blocks this combat if able."));
                }
                log.info("Game {} - {} attacks or blocks this combat if able (attacking: {})",
                        gameData.id, target.getCard().getName(), controllerIsActivePlayer);
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

    /**
     * Stamps the requirement on every creature the controller controls, without targeting any of them
     * (Joraga Invocation). The per-creature log lines of the targeted branches would flood the log here,
     * so the sweep appends a single line naming the source instead.
     */
    private void resolveForOwnCreatures(GameData gameData, StackEntry entry, CombatRequirement requirement) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }

            stamp(permanent, requirement, entry.getControllerId());
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " applies its combat requirement to " + count + " creature(s)."));
        log.info("Game {} - {} applied {} to {} own creature(s)", gameData.id, entry.getCard().getName(),
                requirement, count);
    }

    private void stamp(Permanent permanent, CombatRequirement requirement, UUID controllerId) {
        switch (requirement) {
            case MUST_ATTACK -> permanent.setMustAttackThisTurn(true);
            case MUST_ATTACK_EFFECT_CONTROLLER -> {
                permanent.setMustAttackThisTurn(true);
                permanent.setMustAttackTargetId(controllerId);
            }
            case MUST_BLOCK -> permanent.setMustBlockThisTurnIfAble(true);
            case MUST_BE_BLOCKED -> permanent.setMustBeBlockedThisTurn(true);
            case MUST_BE_BLOCKED_BY_ALL -> permanent.setMustBeBlockedByAllThisTurn(true);
        }
    }
}
