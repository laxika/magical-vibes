package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
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
    private final GameQueryService gameQueryService;

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

        // Create a copy Card with a new identity but same properties
        Card copyCard = createCopyCard(targetEntry.getCard());

        // Create a copy of the stack entry preserving all fields, with the copy's controller
        UUID copyControllerId = entry.getControllerId();
        StackEntry copyEntry = new StackEntry(
                targetEntry.getEntryType(),
                copyCard,
                copyControllerId,
                "Copy of " + targetEntry.getCard().getName(),
                new ArrayList<>(targetEntry.getEffectsToResolve()),
                targetEntry.getXValue(),
                targetEntry.getTargetPermanentId(),
                targetEntry.getSourcePermanentId(),
                targetEntry.getDamageAssignments(),
                targetEntry.getTargetZone(),
                targetEntry.getTargetCardIds() != null ? new ArrayList<>(targetEntry.getTargetCardIds()) : null,
                targetEntry.getTargetPermanentIds() != null ? new ArrayList<>(targetEntry.getTargetPermanentIds()) : null
        );
        copyEntry.setCopy(true);

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

            // Check if the spell could legally target this permanent
            if (spellCard.getTargetFilter() instanceof PermanentPredicateTargetFilter ppf) {
                if (!gameQueryService.matchesPermanentPredicate(gameData, perm, ppf.predicate())) {
                    return;
                }
            }
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) return;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) {
                UUID permController = gameQueryService.findPermanentController(gameData, perm.getId());
                if (permController != null && !permController.equals(castingPlayerId)) return;
            }
            if (gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
                UUID permController = gameQueryService.findPermanentController(gameData, perm.getId());
                if (permController != null && !permController.equals(castingPlayerId)) return;
            }
            if (spellCard.getColor() != null && gameQueryService.cantBeTargetedBySpellColor(gameData, perm, spellCard.getColor())) {
                return;
            }
            if (spellCard.getColor() != null && gameQueryService.hasProtectionFrom(gameData, perm, spellCard.getColor())) {
                return;
            }

            eligibleTargets.add(perm);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = createCopyCard(spellCard);

            StackEntry copyEntry = new StackEntry(
                    spellSnapshot.getEntryType(),
                    copyCard,
                    castingPlayerId,
                    "Copy of " + spellCard.getName(),
                    new ArrayList<>(spellSnapshot.getEffectsToResolve()),
                    spellSnapshot.getXValue(),
                    target.getId(),
                    spellSnapshot.getSourcePermanentId(),
                    spellSnapshot.getDamageAssignments(),
                    spellSnapshot.getTargetZone(),
                    spellSnapshot.getTargetCardIds() != null ? new ArrayList<>(spellSnapshot.getTargetCardIds()) : null,
                    spellSnapshot.getTargetPermanentIds() != null ? new ArrayList<>(spellSnapshot.getTargetPermanentIds()) : null
            );
            copyEntry.setCopy(true);

            gameData.stack.add(copyEntry);

            String logMsg = "A copy of " + spellCard.getName() + " is created targeting " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other {}",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(),
                spellCard.getName(), subtype.getDisplayName());
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

