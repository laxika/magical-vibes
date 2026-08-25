package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyActivatedAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
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

    private static final Set<StackEntryType> COPYABLE_STACK_TYPES = Set.of(
            StackEntryType.INSTANT_SPELL,
            StackEntryType.SORCERY_SPELL,
            StackEntryType.ACTIVATED_ABILITY,
            StackEntryType.TRIGGERED_ABILITY);

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

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
        if (targetEntry.getCard().isCantBeCopied()) {
            gameLogService.append(gameData, GameLog.cardThen(targetEntry.getCard(), " can't be copied."));
            return;
        }

        Card copyCard = copySupport.createCopyCard(targetEntry.getCard());
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
        gameData.stack.add(copyEntry);

        gameLogService.append(gameData, GameLog.textCardText(
                "A copy of ", targetEntry.getCard(), " is created."));
        queueRetargetChoice(gameData, entry, targetEntry, copyCard);
    }

    private StackEntry findTargetEntry(GameData gameData, UUID targetCardId, StackEntryPredicate predicate) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)
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
            default -> true;
        };
    }

    private void queueRetargetChoice(GameData gameData, StackEntry entry,
                                     StackEntry targetEntry, Card copyCard) {
        if (!targetEntry.isSingleTarget()) {
            return;
        }

        CardEffect retargetEffect;
        if (targetEntry.getEntryType() == StackEntryType.INSTANT_SPELL
                || targetEntry.getEntryType() == StackEntryType.SORCERY_SPELL) {
            retargetEffect = new CopySpellEffect();
        } else if (targetEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            ActivatedAbility syntheticAbility = new ActivatedAbility(
                    false, null, List.copyOf(targetEntry.getEffectsToResolve()),
                    "copy retarget", targetEntry.getTargetFilter());
            retargetEffect = new CopyActivatedAbilityRetargetEffect(
                    syntheticAbility, targetEntry.getSourcePermanentId());
        } else {
            retargetEffect = new CopyTriggeredAbilityRetargetEffect();
        }

        PendingMayAbility retargetAbility = new PendingMayAbility(
                entry.getCard(), entry.getControllerId(), List.of(retargetEffect),
                "Choose a new target for the copy of " + targetEntry.getCard().getName() + "?",
                copyCard.getId());
        gameData.pendingMayAbilities.addFirst(retargetAbility);
    }
}
