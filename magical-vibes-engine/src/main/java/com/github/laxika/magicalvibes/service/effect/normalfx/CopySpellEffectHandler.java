package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import java.util.List;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService triggerCollectionService;
    private final TargetLegalityService targetLegalityService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopySpellEffect copyEffect = (CopySpellEffect) effect;
        // A graveyard ability's triggering card id names its own source card, not a spell to copy, so it
        // copies its target (Geistblast).
        UUID targetCardId = entry.getTriggeringCardId() != null && entry.getSourceZone() != Zone.GRAVEYARD
                ? entry.getTriggeringCardId() : entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Copy target no longer on stack", gameData.id);
            return;
        }

        // CR 608.2b — the target must still satisfy the spell filter when the copy is made (e.g. the
        // spell is no longer controlled by the copying effect's controller).
        if (copyEffect.spellFilter() != null
                && !targetLegalityService.matchesStackEntryPredicate(gameData, targetEntry,
                copyEffect.spellFilter(), entry.getControllerId())) {
            log.info("Game {} - Copy target no longer matches the copy filter", gameData.id);
            return;
        }

        // CR 707.10 — "This spell can't be copied": the copy is simply not created.
        if (targetEntry.getCard().isCantBeCopied()) {
            gameLogService.append(gameData, GameLog.cardThen(targetEntry.getCard(), " can't be copied."));
            log.info("Game {} - {} can't be copied", gameData.id, targetEntry.getCard().getName());
            return;
        }

        UUID copyControllerId = copyEffect.copyForTargetController()
                ? targetEntry.getControllerId()
                : entry.getControllerId();
        int copyCount = copyEffect.copies() <= 1 ? 1 : copySupport.adjustedSpellCopyCount(gameData, copyControllerId, copyEffect.copies());
        for (int copyIndex = 0; copyIndex < copyCount; copyIndex++) {
            createCopy(gameData, entry, copyEffect, targetEntry, copyControllerId);
        }
    }

    private void createCopy(GameData gameData, StackEntry entry, CopySpellEffect copyEffect,
                            StackEntry targetEntry, UUID copyControllerId) {
        Card copyCard = copySupport.createCopyCard(targetEntry.getCard());
        if (copyEffect.removeLegendary()) {
            var supertypes = EnumSet.noneOf(CardSupertype.class);
            supertypes.addAll(copyCard.getSupertypes());
            supertypes.remove(CardSupertype.LEGENDARY);
            copyCard.setSupertypes(supertypes);
        }
        if (copyEffect.colorOverride() != null) {
            copyCard.setColor(copyEffect.colorOverride());
            copyCard.setColors(List.of(copyEffect.colorOverride()));
        }
        // Token-copy modes mark the copy before it resolves into a permanent. The creature-copy
        // mode additionally grants haste and may register a delayed sacrifice.
        if (copyEffect.tokenCopy() || copyEffect.tokenWithHaste()
                || (copyEffect.permanentSpellToken() && isPermanentSpell(targetEntry))) {
            copyCard.setToken(true);
        }
        if (copyEffect.tokenWithHaste()) {
            copyCard.setSacrificeAtEndStep(copyEffect.sacrificeAtEndStep());
        }
        StackEntry copyEntry = copySupport.createCopyStackEntry(targetEntry, copyCard, copyControllerId, targetEntry.getTargetId());
        if (copyEffect.tokenWithHaste()) {
            // The copy *gains* haste — it is not printed on the copied card. Carrying it on the
            // entry (drained into the permanent's granted keywords at resolution) keeps it out of
            // the copied card's printed keywords, so the UI shows it as granted.
            copyEntry.getGrantedKeywordsOnEntry().add(Keyword.HASTE);
        }

        // Only the instant/sorcery-copy mode offers "you may choose new targets for the copy"; such a copy
        // waits off the stack until its targets are decided (CR 707.10c).
        boolean offersNewTargets = !copyEffect.tokenCopy() && !copyEffect.tokenWithHaste()
                && !copyEffect.permanentSpellToken()
                && (copyEntry.getTargetId() != null || !copyEntry.getTargetIds().isEmpty()
                || !copyEntry.getTargetCardIds().isEmpty());
        PendingMayAbility retargetAbility = offersNewTargets
                ? new PendingMayAbility(
                        entry.getCard(),
                        copyControllerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + targetEntry.getCard().getName() + "?",
                        copyCard.getId())
                : null;
        copySupport.addCopyToStack(gameData, copyEntry, copyEffect.copies() <= 1, retargetAbility);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", targetEntry.getCard(), " is created."));
        log.info("Game {} - {} copies {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());

        // A copy put onto the stack with the original's targets makes each of those objects the target of a
        // spell the copy's controller controls. A staged copy does so only when it is committed with its
        // final targets.
        if (!offersNewTargets && (copyEntry.getTargetId() != null || !copyEntry.getTargetIds().isEmpty())) {
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, copyEntry);
        }
    }

    private static boolean isPermanentSpell(StackEntry entry) {
        return switch (entry.getEntryType()) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            default -> false;
        };
    }
}
