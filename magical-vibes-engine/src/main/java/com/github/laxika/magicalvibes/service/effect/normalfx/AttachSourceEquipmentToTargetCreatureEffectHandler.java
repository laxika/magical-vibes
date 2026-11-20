package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Attaches the effect's Equipment to its host, both named by {@link SubjectResolutionService}. An Equipment that can't
 * be attached to the host stays where it is (CR 301.5c, 701.3b); one already attached to it doesn't move, but still
 * counts as attached for the reflexive continuation. A moved Equipment gets a new timestamp (CR 613.7e), its
 * unattach triggers and "becomes attached" triggers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final DurationClock durationClock;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attachEffect = (AttachSourceEquipmentToTargetCreatureEffect) effect;
        List<Permanent> equipments = subjectResolutionService.permanents(gameData, entry, effect,
                attachEffect.equipment());
        if (equipments.isEmpty()) {
            if (!attachEffect.equipment().declaresTarget()) {
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s attach ability fizzles (the Equipment is no longer on the battlefield)."));
                log.info("Game {} - Attach equipment fizzles, equipment left battlefield", gameData.id);
            }
            return;
        }
        Permanent host = subjectResolutionService.permanent(gameData, entry, effect, attachEffect.host());
        if (host == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s attach ability fizzles (the creature is no longer on the battlefield)."));
            log.info("Game {} - Attach equipment fizzles, host left battlefield", gameData.id);
            return;
        }

        boolean attached = false;
        for (Permanent equipment : equipments) {
            if (!equipSupport.canAttachEquipment(gameData, equipment, host)) {
                continue;
            }
            attached = true;
            UUID oldAttachedTo = equipment.getAttachedTo();
            if (host.getId().equals(oldAttachedTo)) {
                continue;
            }
            durationClock.sourceUnattached(gameData, equipment.getId());
            equipment.setAttachedTo(host.getId());
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
            equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
            log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(),
                    host.getCard().getName(), entry.getCard().getName());
        }

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
