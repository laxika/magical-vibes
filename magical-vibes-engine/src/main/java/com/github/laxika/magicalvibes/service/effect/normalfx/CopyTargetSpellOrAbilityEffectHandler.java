package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCopyPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTargetSpellOrAbilityEffectHandler implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> COPYABLE_STACK_TYPES = StackEntryType.abilitiesAnd(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL);

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final PsychicBattleSupport psychicBattleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTargetSpellOrAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyTargetSpellOrAbilityEffect copyEffect = (CopyTargetSpellOrAbilityEffect) effect;
        StackEntry targetEntry = findTargetEntry(gameData, entry.getTargetId(), copyEffect.targetPredicate());
        if (targetEntry == null) {
            log.info("Game {} - Copy target spell or ability is no longer on the stack", gameData.id);
            return;
        }
        if (CopySupport.isAbilityThatCantBeCopied(targetEntry)) {
            gameLogService.append(gameData, GameLog.text(
                    FaceDownVisibility.descriptionShownTo(gameData, targetEntry, null) + " can't be copied."));
            return;
        }
        if (targetEntry.getCard() != null && targetEntry.getCard().isCantBeCopied()) {
            gameLogService.append(gameData, GameLog.cardThen(targetEntry.getCard(), " can't be copied."));
            return;
        }

        Card copyCard = targetEntry.getCard() == null ? null : copySupport.createCopyCard(targetEntry);
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                targetEntry, copyCard, entry.getControllerId(), targetEntry.getTargetId());
        copyEntry.setTargetFilter(targetEntry.getTargetFilter());
        copyEntry.setDamageSourceCard(targetEntry.getDamageSourceCard());
        copyEntry.setTargetGroupSizes(targetEntry.getTargetGroupSizes());
        copyEntry.setPrimaryTargetStoredSeparately(targetEntry.isPrimaryTargetStoredSeparately());
        copyEntry.setTriggeringPermanentId(targetEntry.getTriggeringPermanentId());
        copyEntry.setTriggeringPermanentControllerId(targetEntry.getTriggeringPermanentControllerId());
        copyEntry.setTriggeringCardId(targetEntry.getTriggeringCardId());
        copyEntry.setEventValue(targetEntry.getEventValue());
        copyEntry.setSourcePermanentSnapshot(targetEntry.getSourcePermanentSnapshot());
        copyEntry.setNonTargeting(targetEntry.isNonTargeting());
        copyEntry.setChosenPermanentId(targetEntry.getChosenPermanentId());
        copyEntry.setAttackedTargetId(targetEntry.getAttackedTargetId());
        PendingMayAbility retargetAbility = retargetChoice(gameData, entry, targetEntry, copyEntry);
        boolean abilityCopy = copyEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || copyEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
        if (abilityCopy) {
            copySupport.addCopyToStack(gameData, copyEntry);
            if (retargetAbility != null) {
                gameData.pendingMayAbilities.addFirst(retargetAbility);
            }
        } else {
            // A spell copy waits off the stack until its new targets are decided (CR 707.10c).
            copySupport.addCopyToStack(gameData, copyEntry, retargetAbility);
        }

        gameLogService.append(gameData, GameLog.text("A copy of "
                + FaceDownVisibility.descriptionShownTo(gameData, targetEntry, null) + " is created."));
    }

    private StackEntry findTargetEntry(GameData gameData, UUID targetCardId, StackEntryPredicate predicate) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.isIdentifiedBy(targetCardId)
                    && COPYABLE_STACK_TYPES.contains(stackEntry.getEntryType())
                    && matchesTargetPredicate(stackEntry, predicate)) {
                return stackEntry;
            }
        }
        return null;
    }

    private boolean matchesTargetPredicate(StackEntry stackEntry, StackEntryPredicate predicate) {
        return switch (predicate) {
            case StackEntryTypeInPredicate typeIn -> typeIn.spellTypes().contains(stackEntry.getEntryType());
            case StackEntryAllOfPredicate allOf ->
                    allOf.predicates().stream().allMatch(p -> matchesTargetPredicate(stackEntry, p));
            case StackEntryAnyOfPredicate anyOf ->
                    anyOf.predicates().stream().anyMatch(p -> matchesTargetPredicate(stackEntry, p));
            case StackEntryIsSingleTargetPredicate ignored -> stackEntry.isSingleTarget();
            case StackEntryIsCopyPredicate copyPredicate -> copyPredicate.castCopy()
                    ? stackEntry.isCopy() && stackEntry.isCastCopy()
                    : stackEntry.isCopy() && !stackEntry.isCastCopy();
            default -> true;
        };
    }

    private PendingMayAbility retargetChoice(GameData gameData, StackEntry entry,
                                             StackEntry targetEntry, StackEntry copyEntry) {
        if (!targetEntry.isSingleTarget()) {
            if (!copyEntry.isNonTargeting() && !copyEntry.getDeclaredTargetIds().isEmpty()) {
                return psychicBattleSupport.nextChoicePrompt(gameData, entry.getCard(), entry.getControllerId(),
                        copyEntry, 0);
            }
            return null;
        }

        CardEffect retargetEffect;
        if (targetEntry.getEntryType() == StackEntryType.INSTANT_SPELL
                || targetEntry.getEntryType() == StackEntryType.SORCERY_SPELL) {
            retargetEffect = new CopySpellEffect();
        } else {
            // An ability's copy has its targets recomputed from its own effects and target filter.
            retargetEffect = new CopyAbilityRetargetEffect();
        }

        return new PendingMayAbility(
                entry.getCard(), entry.getControllerId(), List.of(retargetEffect),
                "Choose a new target for the copy of " + FaceDownVisibility.descriptionShownTo(gameData, targetEntry, entry.getControllerId()) + "?",
                copyEntry.getTargetableId());
    }
}
