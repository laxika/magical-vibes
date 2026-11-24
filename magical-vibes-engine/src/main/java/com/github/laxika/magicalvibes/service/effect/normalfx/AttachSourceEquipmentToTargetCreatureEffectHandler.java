package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The attach verb: attaches the effect's Auras, Equipment and Fortifications to its host permanent or player, all named
 * by {@link SubjectResolutionService}. One that can't be attached to the host stays where it is, and one already
 * attached to it doesn't move but still counts as attached for the reflexive continuation (CR 701.3b); the legality
 * and the move are {@link EquipSupport#canAttach} and {@link EquipSupport#attach} (a new timestamp, CR 613.7e, the
 * unattach and "becomes attached" events, control recomputed). Records {@link StepKind#ATTACH}: the attachments that
 * moved and their controllers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attachEffect = (AttachSourceEquipmentToTargetCreatureEffect) effect;
        entry.recordStep(StepKind.ATTACH, StepOutcome.NONE);
        List<Permanent> attachments = subjectResolutionService.permanents(gameData, entry, effect,
                attachEffect.equipment());
        if (attachments.isEmpty()) {
            if (attachEffect.equipment() instanceof PermanentSubject.Source) {
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s attach ability fizzles (the attachment is no longer on the battlefield)."));
                log.info("Game {} - Attach fizzles, attachment left battlefield", gameData.id);
            }
            return;
        }
        Permanent host = null;
        UUID playerHostId = null;
        if (attachEffect.playerHost() != null) {
            playerHostId = subjectResolutionService.player(gameData, entry, effect, attachEffect.playerHost());
            if (playerHostId == null) {
                return;
            }
        } else {
            host = subjectResolutionService.permanent(gameData, entry, effect, attachEffect.host());
            if (host == null) {
                if (!(attachEffect.host() instanceof PermanentSubject.ThisWay)
                        && !(attachEffect.host() instanceof PermanentSubject.ReturnedThisResolution)) {
                    gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                            "'s attach ability fizzles (the creature is no longer on the battlefield)."));
                    log.info("Game {} - Attach fizzles, host left battlefield", gameData.id);
                }
                return;
            }
        }

        boolean attached = false;
        List<UUID> moved = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        for (Permanent attachment : attachments) {
            boolean legal = host != null
                    ? equipSupport.canAttach(gameData, attachment, host)
                    : equipSupport.canAttachToPlayer(gameData, attachment, playerHostId);
            if (!legal) {
                continue;
            }
            attached = true;
            UUID hostId = host != null ? host.getId() : playerHostId;
            if (!equipSupport.attach(gameData, attachment, hostId)) {
                continue;
            }
            moved.add(attachment.getId());
            UUID controllerId = gameData.findControllerOf(attachment);
            if (controllerId != null) {
                controllers.add(controllerId);
            }
            if (host != null) {
                gameLogService.append(gameData,
                        GameLog.cardTextCard(attachment.getCard(), " is now attached to ", host.getCard(), "."));
            } else {
                gameLogService.append(gameData, GameLog.text(FaceDownVisibility.publicName(gameData, attachment)
                        + " is now attached to " + gameData.playerIdToName.get(playerHostId) + "."));
            }
            log.info("Game {} - {} attached to {} via {}", gameData.id, attachment.getCard().getName(), hostId,
                    entry.getCard().getName());
        }
        entry.recordStep(StepKind.ATTACH, moved.isEmpty() ? StepOutcome.NONE
                : StepOutcome.ofObjects(moved, controllers));

        if (attached && attachEffect.thenEffect() != null) {
            int effectIndex = findEffectIndex(entry, effect);
            if (effectIndex < 0) {
                throw new IllegalStateException(
                        "AttachSourceEquipmentToTargetCreatureEffect is not part of the resolving entry");
            }
            entry.insertEffectsToResolve(effectIndex + 1, List.of(
                    QueueReflexiveAbilityEffect.of(attachEffect.thenEffect(), attachEffect.thenEffectOptionalTarget())));
        }
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        int directIndex = entry.getEffectsToResolve().indexOf(effect);
        if (directIndex >= 0) {
            return directIndex;
        }
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect parent = entry.getEffectsToResolve().get(i);
            if (parent instanceof ConditionalEffect conditional && conditional.wrapped() == effect) {
                return i;
            }
            if (parent instanceof MayEffect may
                    && (may.wrapped() == effect || may.elseEffect() == effect)) {
                return i;
            }
        }
        return -1;
    }
}
