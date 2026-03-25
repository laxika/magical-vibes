package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantInstantSorceryCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopyResolutionService {

    private final GameBroadcastService gameBroadcastService;
    private final ValidTargetService validTargetService;
    private final GameQueryService gameQueryService;
    private final CloneService cloneService;

    @HandlesEffect(CopySpellEffect.class)
    void resolveCopySpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Copy target no longer on stack", gameData.id);
            return;
        }

        // Create a copy of the stack entry preserving all fields, with the copy's controller
        UUID copyControllerId = entry.getControllerId();
        Card copyCard = createCopyCard(targetEntry.getCard());
        StackEntry copyEntry = createCopyStackEntry(targetEntry, copyCard, copyControllerId, targetEntry.getTargetId());

        gameData.stack.add(copyEntry);

        String logMsg = "A copy of " + targetEntry.getCard().getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} copies {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());

        // If the copy has a target, offer the controller a chance to choose new targets
        if (copyEntry.getTargetId() != null) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + targetEntry.getCard().getName() + "?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }

    @HandlesEffect(CopySpellForEachOtherSubtypePermanentEffect.class)
    void resolveCopyForEachOtherSubtype(GameData gameData, StackEntry entry,
                                        CopySpellForEachOtherSubtypePermanentEffect effect) {
        if (effect.spellSnapshot() == null) return;

        StackEntry spellSnapshot = effect.spellSnapshot();
        UUID castingPlayerId = effect.castingPlayerId();
        UUID originalTargetId = effect.originalTargetId();
        CardSubtype subtype = effect.subtype();
        Card spellCard = spellSnapshot.getCard();

        // Find all permanents with the matching subtype, excluding the originally targeted one
        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((pid, perm) -> {
            if (perm.getId().equals(originalTargetId)) return;
            if (!perm.getCard().getSubtypes().contains(subtype)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId)) return;

            eligibleTargets.add(perm);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = createCopyCard(spellCard);
            StackEntry copyEntry = createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId());

            gameData.stack.add(copyEntry);

            String logMsg = "A copy of " + spellCard.getName() + " is created targeting " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other {}",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(),
                spellCard.getName(), subtype.getDisplayName());
    }

    @HandlesEffect(CopySpellForEachOtherPlayerEffect.class)
    void resolveCopyForEachOtherPlayer(GameData gameData, StackEntry entry,
                                       CopySpellForEachOtherPlayerEffect effect) {
        if (effect.spellSnapshot() == null) return;

        StackEntry spellSnapshot = effect.spellSnapshot();
        UUID castingPlayerId = effect.castingPlayerId();
        Card spellCard = spellSnapshot.getCard();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(castingPlayerId)) continue;

            Card copyCard = createCopyCard(spellCard);
            StackEntry copyEntry = createCopyStackEntry(spellSnapshot, copyCard, playerId, spellSnapshot.getTargetId());

            gameData.stack.add(copyEntry);

            String logMsg = "A copy of " + spellCard.getName() + " is created for "
                    + gameData.playerIdToName.get(playerId) + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);

            // If the copy has a target, offer the controller a chance to choose new targets
            if (copyEntry.getTargetId() != null) {
                PendingMayAbility retargetAbility = new PendingMayAbility(
                        entry.getCard(),
                        playerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + spellCard.getName() + "?",
                        copyCard.getId()
                );
                gameData.pendingMayAbilities.addFirst(retargetAbility);
            }
        }

        log.info("Game {} - {} triggers, copying {} for each other player",
                gameData.id, entry.getCard().getName(), spellCard.getName());
    }

    @HandlesEffect(GrantInstantSorceryCopyUntilEndOfTurnEffect.class)
    void resolveGrantInstantSorceryCopy(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        gameData.playersWithSpellCopyUntilEndOfTurn.add(controllerId);

        String logMsg = gameData.playerIdToName.get(controllerId)
                + "'s instant and sorcery spells will be copied for the rest of the turn.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} granted spell copy until end of turn", gameData.id, controllerId);
    }

    @HandlesEffect(CopyControllerCastSpellEffect.class)
    void resolveCopyControllerCastSpell(GameData gameData, StackEntry entry,
                                        CopyControllerCastSpellEffect effect) {
        if (effect.spellSnapshot() == null) return;

        StackEntry spellSnapshot = effect.spellSnapshot();
        UUID castingPlayerId = effect.castingPlayerId();
        Card spellCard = spellSnapshot.getCard();

        Card copyCard = createCopyCard(spellCard);
        StackEntry copyEntry = createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, spellSnapshot.getTargetId());

        gameData.stack.add(copyEntry);

        String logMsg = "A copy of " + spellCard.getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - copy of {} created for controller", gameData.id, spellCard.getName());

        // If the copy has a target, offer the controller a chance to choose new targets
        if (copyEntry.getTargetId() != null) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    castingPlayerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + spellCard.getName() + "?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }

    @HandlesEffect(BecomeCopyOfTargetCreatureEffect.class)
    void resolveBecomeCopyOfTargetCreature(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPerm == null) {
            log.info("Game {} - Become-copy target no longer exists", gameData.id);
            return;
        }

        // CR 603.5: The "may" choice happens at resolution time.
        // Queue a pending may ability so the controller can choose whether to become a copy.
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new BecomeCopyOfTargetCreatureEffect()),
                entry.getCard().getName() + " — You may have this creature become a copy of " + targetPerm.getCard().getName() + ".",
                targetId
        ));
        log.info("Game {} - {} become-copy may choice queued for target {}",
                gameData.id, entry.getCard().getName(), targetPerm.getCard().getName());
    }

    @HandlesEffect(BecomeCopyOfTargetCreatureUntilEndOfTurnEffect.class)
    void resolveBecomeCopyOfTargetCreatureUntilEndOfTurn(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPerm == null) {
            log.info("Game {} - Become-copy-until-end-of-turn target no longer exists", gameData.id);
            return;
        }

        // Find the source permanent
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            log.info("Game {} - Become-copy-until-end-of-turn source no longer on battlefield", gameData.id);
            return;
        }

        // Save the current card for end-of-turn revert (only if not already a temporary copy this turn)
        if (!sourcePermanent.isCopyUntilEndOfTurn()) {
            sourcePermanent.setPreCopyCard(sourcePermanent.getCard());
        }

        String originalName = sourcePermanent.getCard().getName();
        cloneService.applyCloneCopy(sourcePermanent, targetPerm, null, null);
        sourcePermanent.setCopyUntilEndOfTurn(true);

        String targetName = targetPerm.getCard().getName();
        String logMsg = originalName + " becomes a copy of " + targetName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} becomes a copy of {} until end of turn", gameData.id, originalName, targetName);
    }

    private StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId, UUID targetId) {
        StackEntry copy = new StackEntry(
                source.getEntryType(),
                copyCard,
                controllerId,
                "Copy of " + source.getCard().getName(),
                new ArrayList<>(source.getEffectsToResolve()),
                source.getXValue(),
                targetId,
                source.getSourcePermanentId(),
                source.getDamageAssignments(),
                source.getTargetZone(),
                source.getTargetCardIds() != null ? new ArrayList<>(source.getTargetCardIds()) : null,
                source.getTargetIds() != null ? new ArrayList<>(source.getTargetIds()) : null
        );
        copy.setCopy(true);
        return copy;
    }

    private Card createCopyCard(Card original) {
        Card copy = new Card();

        copy.setName(original.getName());
        copy.setType(original.getType());
        copy.setManaCost(original.getManaCost());
        copy.setColor(original.getColor());
        copy.setSupertypes(original.getSupertypes());
        copy.setSubtypes(original.getSubtypes());
        copy.setCardText(original.getCardText());
        copy.setPower(original.getPower());
        copy.setToughness(original.getToughness());
        copy.setKeywords(original.getKeywords());
        copy.setLoyalty(original.getLoyalty());
        copy.setXColorRestriction(original.getXColorRestriction());

        // Target validation for copied spells relies on card-level spell effects.
        for (EffectSlot slot : EffectSlot.values()) {
            for (var reg : original.getEffectRegistrations(slot)) {
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }

        // Copy targeting configuration (SpellTargets, effect-target mapping, modal overrides)
        copy.copyTargetingFrom(original);

        return copy;
    }
}
