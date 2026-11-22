package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetCombatRequirementThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetCombatRequirementThisTurnEffect) effect;
        if (e.subject().isSet()) {
            resolveForOwnCreatures(gameData, entry, e, e.requirement());
            return;
        }

        // allTargets() is every target of the effect's multi-target group; target() the first; attachedCreature() the
        // Aura's host as it resolves, so moving the Aura in response redirects the requirement.
        boolean everyTarget = e.subject() instanceof PermanentSubject.AllTargets;
        List<Permanent> recipients = subjectResolutionService.permanents(gameData, entry, effect,
                everyTarget ? Subjects.target() : e.subject());
        if (!everyTarget && !(e.subject() instanceof PermanentSubject.ThisWay) && recipients.size() > 1) {
            recipients = recipients.subList(0, 1);
        }
        for (Permanent target : recipients) {
            resolveForTarget(gameData, entry, e.requirement(), target);
        }
    }

    private void resolveForTarget(GameData gameData, StackEntry entry,
                                  CombatRequirement requirement, Permanent target) {
        switch (requirement) {
            case MUST_ATTACK -> {
                stamp(gameData, entry, target, requirement);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " must attack this turn if able."));
                log.info("Game {} - {} must attack this turn if able", gameData.id, target.getCard().getName());
            }
            case MUST_ATTACK_EFFECT_CONTROLLER -> {
                // Force the creature to attack the ability's controller specifically, not their planeswalkers
                // (Scryfall ruling: "it must attack you, not the planeswalker")
                stamp(gameData, entry, target, requirement);

                String controllerName = gameData.playerIdToName.get(entry.getControllerId());

                gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" must attack " + controllerName + " this turn if able.").build());
                log.info("Game {} - {} must attack {} this turn if able", gameData.id, target.getCard().getName(), controllerName);
            }
            case MUST_BLOCK -> {
                stamp(gameData, entry, target, requirement);

                String logEntry = FaceDownVisibility.publicName(gameData, target) + " blocks this turn if able.";
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
                    stamp(gameData, entry, target, CombatRequirement.MUST_ATTACK);
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " attacks this combat if able."));
                } else {
                    stamp(gameData, entry, target, CombatRequirement.MUST_BLOCK);
                    gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " blocks this combat if able."));
                }
                log.info("Game {} - {} attacks or blocks this combat if able (attacking: {})",
                        gameData.id, target.getCard().getName(), controllerIsActivePlayer);
            }
            case MUST_BE_BLOCKED -> {
                stamp(gameData, entry, target, requirement);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " must be blocked this turn if able."));
                log.info("Game {} - {} must be blocked this turn if able", gameData.id, target.getCard().getName());
            }
            case MUST_BE_BLOCKED_BY_ALL -> {
                stamp(gameData, entry, target, requirement);
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
    private void resolveForOwnCreatures(GameData gameData, StackEntry entry, SetCombatRequirementThisTurnEffect effect,
                                        CombatRequirement requirement) {
        int count = 0;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, effect.subject())) {
            stamp(gameData, entry, permanent, requirement);
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " applies its combat requirement to " + count + " creature(s)."));
        log.info("Game {} - {} applied {} to {} own creature(s)", gameData.id, entry.getCard().getName(),
                requirement, count);
    }

    /** Puts {@code requirement} on {@code permanent} until end of turn, as a floating effect of the resolving entry. */
    private void stamp(GameData gameData, StackEntry entry, Permanent permanent, CombatRequirement requirement) {
        CardEffect payload = switch (requirement) {
            case MUST_ATTACK -> new MustAttackIfAbleEffect();
            case MUST_ATTACK_EFFECT_CONTROLLER -> new MustAttackIfAbleEffect(entry.getControllerId());
            case MUST_BLOCK -> new MustBlockEachCombatEffect();
            case MUST_BE_BLOCKED -> new MustBeBlockedIfAbleEffect();
            case MUST_BE_BLOCKED_BY_ALL -> new MustBeBlockedByAllCreaturesEffect();
            case MUST_ATTACK_OR_BLOCK -> throw new IllegalArgumentException("Resolve attack-or-block to one half first");
        };
        gameData.addEffectOnPermanent(entry, payload, permanent.getId(), Duration.UNTIL_END_OF_TURN);
    }
}
