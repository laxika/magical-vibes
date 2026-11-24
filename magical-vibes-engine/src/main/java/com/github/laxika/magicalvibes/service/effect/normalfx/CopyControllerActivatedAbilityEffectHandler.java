package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CopyControllerActivatedAbilityEffect}: a copy of the snapshotted activated or triggered ability on
 * the stack (CR 707.10) for its controller. {@link #copy} is the one way an ability is copied, for the card-made copies
 * ({@code CopyTargetActivatedOrTriggeredAbilityEffectHandler}) and a card's "copy that ability"
 * ({@link com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect}, through its handler) too.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyControllerActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final PsychicBattleSupport psychicBattleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyControllerActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopyControllerActivatedAbilityEffect) effect;
        copy(gameData, entry, e.abilitySnapshot(), e.activatingPlayerId(), e.ability());
    }

    /**
     * Puts a copy of {@code original} on the stack under {@code copyControllerId}'s control with every decision made
     * for the original: its targets and their groups, X, the objects used to pay its costs (the convoking creatures,
     * the permanent tapped or chosen for it), its source as it last existed (CR 707.10). Its controller may then choose
     * new targets for it (CR 707.10c): one prompt for a single target, else each target in turn. Nothing is copied
     * when {@code original} is missing or can't be copied.
     *
     * @param entry   the resolving spell or ability that copies; its card is the source of the retarget prompt
     * @param ability the activated ability {@code original} was activated from, when known, for the retarget prompt's
     *                targets; else they are recomputed from the copy's own effects and target filter
     */
    public void copy(GameData gameData, StackEntry entry, StackEntry original, UUID copyControllerId,
                     ActivatedAbility ability) {
        if (original == null || original.getCard() != null && original.getCard().isCantBeCopied()
                || CopySupport.isAbilityThatCantBeCopied(original)) {
            log.info("Game {} - The ability can't be copied", gameData.id);
            return;
        }

        Card copyCard = original.getCard() == null ? null : copySupport.createCopyCard(original.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(original, copyCard, copyControllerId,
                original.getTargetId());
        copyEntry.setTargetFilter(original.getTargetFilter());
        copyEntry.setDamageSourceCard(original.getDamageSourceCard());
        copyEntry.setTargetGroupSizes(original.getTargetGroupSizes());
        copyEntry.setPrimaryTargetStoredSeparately(original.isPrimaryTargetStoredSeparately());
        copyEntry.setConvokeCreatureIds(original.getConvokeCreatureIds());
        copyEntry.setNonTargeting(original.isNonTargeting());
        copyEntry.setChosenPermanentId(original.getChosenPermanentId());

        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.text("A copy of "
                + FaceDownVisibility.descriptionShownTo(gameData, original, null) + " is created."));
        log.info("Game {} - copy of {} created", gameData.id, original.getDescription());

        if (original.isNonTargeting()) {
            return;
        }
        boolean singleTarget = original.getTargetId() != null
                && (original.getTargetIds() == null || original.getTargetIds().size() <= 1)
                && (ability == null || !ability.isMultiTarget());
        if (psychicBattleSupport.targetIds(copyEntry).size() > 1
                || !singleTarget && !copyEntry.getDeclaredTargetIds().isEmpty()) {
            psychicBattleSupport.queueNextChoice(gameData, entry.getCard(), copyControllerId,
                    copyEntry.getTargetableId(), 0);
            return;
        }
        if (singleTarget) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(new CopyAbilityRetargetEffect(ability)),
                    "Choose a new target for the copy of "
                            + FaceDownVisibility.descriptionShownTo(gameData, original, copyControllerId)
                            + "'s ability?",
                    copyEntry.getTargetableId()));
        }
    }
}
