package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.trigger.pipeline.AttachmentTriggers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Shared equip and attach helpers. {@link #canAttach} and {@link #attach} are the attach verb's legality and move for
 * any Aura, Equipment or Fortification (CR 701.3); the {@code canAttachEquipment} forms are the Equipment part.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final UnattachTriggerSupport unattachTriggerSupport;
    private final AttachmentTriggers attachmentTriggers;
    private final DurationClock durationClock;
    private final AuraAttachmentService auraAttachmentService;
    private final CreatureControlService creatureControlService;

    public void notifyEquipmentAttached(GameData gameData, Permanent equipment, UUID oldAttachedTo) {
        attachmentTriggers.equipmentBecameAttached(gameData, equipment, oldAttachedTo);
    }

    public Permanent findEquipmentByCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(cardId)) {
                    return p;
                }
            }
        }
        return null;
    }

    public boolean canAttachEquipment(GameData gameData, Permanent equipment, Permanent host) {
        return canAttachEquipment(gameData, equipment, host, false);
    }

    public boolean canAttachEquipment(GameData gameData, Permanent equipment, Permanent host,
                                      boolean permitsNonCreatureTarget) {
        return canAttachEquipment(gameData, equipment, host, permitsNonCreatureTarget, false);
    }

    public boolean canAttachEquipment(GameData gameData, Permanent equipment, Permanent host,
                                      boolean permitsNonCreatureTarget,
                                      boolean permitsCreatureEquipment) {
        if (equipment.getId().equals(host.getId())
                || !gameQueryService.hasEffectiveSubtype(gameData, equipment, CardSubtype.EQUIPMENT)
                || (!permitsCreatureEquipment && gameQueryService.isCreature(gameData, equipment)
                && !gameQueryService.hasReconfigure(gameData, equipment))
                || (!permitsNonCreatureTarget && !gameQueryService.isCreature(gameData, host))
                || gameQueryService.cantBeEquipped(gameData, host)
                || gameQueryService.hasProtectionFromSource(gameData, host, equipment)) {
            return false;
        }

        var attachRestriction = equipment.getCard().getAttachRestriction();
        return attachRestriction == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, host, attachRestriction);
    }

    public boolean attachEquipment(GameData gameData, Permanent equipment, Permanent host) {
        if (!canAttachEquipment(gameData, equipment, host)) {
            return false;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        if (host.getId().equals(oldAttachedTo)) {
            return true;
        }
        durationClock.sourceUnattached(gameData, equipment.getId());
        equipment.setAttachedTo(host.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
        notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
        return true;
    }

    /**
     * Enqueues the unattach triggers (destroy-on-unattach and sacrifice-on-unattach) when equipment
     * moves off {@code oldAttachedTo}. The previously-attached permanent is only affected when those
     * triggers resolve.
     */
    public void applySacrificeOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                UUID oldAttachedTo, UUID newAttachedTo) {
        if (oldAttachedTo != null && !oldAttachedTo.equals(newAttachedTo)) {
            unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, equipment, oldAttachedTo);
        }
    }

    /**
     * Whether {@code attachment} could be attached to {@code host} (CR 701.3a): an Aura to a permanent it could enchant
     * (its enchant ability, protection, "can't be enchanted"; not itself, not while it is a creature, CR 303.4d), an
     * Equipment to a creature it could equip ({@link #canAttachEquipment}, CR 301.5c), a Fortification to a land it
     * could fortify (not itself, not while it is a creature that isn't a land, protection; CR 301.6, 702.16d). Any other
     * permanent can't be attached (CR 701.3b).
     */
    public boolean canAttach(GameData gameData, Permanent attachment, Permanent host) {
        if (attachment.getId().equals(host.getId())) {
            return false;
        }
        if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.AURA)) {
            UUID controllerId = gameData.findControllerOf(attachment);
            return controllerId != null
                    && !gameQueryService.isCreature(gameData, attachment)
                    && auraAttachmentService.canEnchant(gameData, attachment.getCard(), controllerId, host);
        }
        if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.EQUIPMENT)) {
            return canAttachEquipment(gameData, attachment, host);
        }
        if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.FORTIFICATION)) {
            return gameQueryService.isLand(gameData, host)
                    && (!gameQueryService.isCreature(gameData, attachment) || gameQueryService.isLand(gameData, attachment))
                    && !gameQueryService.hasProtectionFromSource(gameData, host, attachment);
        }
        return false;
    }

    /** Whether {@code attachment} could be attached to the player {@code playerId}: an Aura that could enchant them. */
    public boolean canAttachToPlayer(GameData gameData, Permanent attachment, UUID playerId) {
        if (!gameData.playerIds.contains(playerId)
                || !gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.AURA)
                || gameQueryService.isCreature(gameData, attachment)) {
            return false;
        }
        UUID controllerId = gameData.findControllerOf(attachment);
        return controllerId != null
                && auraAttachmentService.canEnchantPlayer(gameData, attachment.getCard(), controllerId, playerId);
    }

    /**
     * Attaches {@code attachment}, which {@link #canAttach} or {@link #canAttachToPlayer} allowed, to the object or
     * player {@code hostId}: nothing happens when it is already attached there (CR 701.3b); otherwise the effects that
     * last while it stays attached end, it gets a new timestamp (CR 613.7e), its unattach and "becomes attached" events
     * occur, and control is recomputed when it carries a control effect. Returns whether it moved.
     */
    public boolean attach(GameData gameData, Permanent attachment, UUID hostId) {
        UUID oldAttachedTo = attachment.getAttachedTo();
        if (hostId.equals(oldAttachedTo)) {
            return false;
        }
        boolean endedControl = durationClock.sourceUnattached(gameData, attachment.getId()).stream()
                .anyMatch(FloatingContinuousEffect::isControlEffect);
        attachment.setAttachedTo(hostId);
        attachment.setTimestamp(gameData.nextTimestamp());
        if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.EQUIPMENT)) {
            applySacrificeOnUnattachIfNeeded(gameData, attachment, oldAttachedTo, hostId);
            notifyEquipmentAttached(gameData, attachment, oldAttachedTo);
        } else if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.AURA)) {
            gameData.rulesEvents.occur(new AttachmentEvent.AuraAttached(attachment, hostId));
        }
        if (endedControl || attachment.getCard().getStaticEffects().stream()
                .anyMatch(ControlEnchantedCreatureEffect.class::isInstance)) {
            creatureControlService.reconcileControl(gameData);
        }
        return true;
    }
}
