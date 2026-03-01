package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
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

    @HandlesEffect(CopySpellEffect.class)
    void resolveCopySpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
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
        StackEntry copyEntry = createCopyStackEntry(targetEntry, copyCard, copyControllerId, targetEntry.getTargetPermanentId());

        gameData.stack.add(copyEntry);

        String logMsg = "A copy of " + targetEntry.getCard().getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} copies {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());

        // If the copy has a target, offer the controller a chance to choose new targets
        if (copyEntry.getTargetPermanentId() != null) {
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
        UUID originalTargetId = effect.originalTargetPermanentId();
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

    private StackEntry createCopyStackEntry(StackEntry source, Card copyCard, UUID controllerId, UUID targetPermanentId) {
        StackEntry copy = new StackEntry(
                source.getEntryType(),
                copyCard,
                controllerId,
                "Copy of " + source.getCard().getName(),
                new ArrayList<>(source.getEffectsToResolve()),
                source.getXValue(),
                targetPermanentId,
                source.getSourcePermanentId(),
                source.getDamageAssignments(),
                source.getTargetZone(),
                source.getTargetCardIds() != null ? new ArrayList<>(source.getTargetCardIds()) : null,
                source.getTargetPermanentIds() != null ? new ArrayList<>(source.getTargetPermanentIds()) : null
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
        copy.setTargetFilter(original.getTargetFilter());
        copy.setLoyalty(original.getLoyalty());
        copy.setXColorRestriction(original.getXColorRestriction());
        copy.setMinTargets(original.getMinTargets());
        copy.setMaxTargets(original.getMaxTargets());

        // Target validation for copied spells relies on card-level spell effects.
        for (EffectSlot slot : EffectSlot.values()) {
            for (var reg : original.getEffectRegistrations(slot)) {
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }

        return copy;
    }
}

