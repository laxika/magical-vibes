package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeTargetOfTargetSpellToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TargetRedirectionSupport targetRedirectionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        if (!targetSpell.hasAnyTarget()) {
            
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " has no effect (", targetSpell.getCard(), " has no targets)."));
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no effect (source permanent no longer on the battlefield)."));
            return;
        }

        if (targetSpell.isSingleTarget() && targetSpell.getTargetCardIds().isEmpty()) {
            if (sourcePermanentId.equals(onlyTargetOf(targetSpell))) {
                gameLogService.append(gameData,
                        GameLog.cardTextCard(targetSpell.getCard(), " already targets ", entry.getCard(), "."));
                return;
            }
            if (targetRedirectionSupport.isValidNewTargetForSpell(gameData, targetSpell, sourcePermanentId)) {
                changeTarget(gameData, targetSpell, onlyTargetOf(targetSpell), sourcePermanentId);
            } else {
                gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " is not a legal target for ", targetSpell.getCard(), ". Target not changed."));
            }
            return;
        }

        List<UUID> declaredTargets = targetSpell.getDeclaredTargetIds();
        if (targetSpell.getTargetId() == null && targetSpell.getTargetCardIds().isEmpty()
                && declaredTargets.size() > 1) {
            if (!targetRedirectionSupport.isValidNewTargetForSpell(gameData, targetSpell, sourcePermanentId)) {
                gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " is not a legal target for ", targetSpell.getCard(), ". Target not changed."));
                return;
            }
            List<UUID> changeable = declaredTargets.stream()
                    .filter(id -> !id.equals(sourcePermanentId))
                    .distinct()
                    .toList();
            if (changeable.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.cardTextCard(targetSpell.getCard(), " already targets ", entry.getCard(), "."));
                return;
            }
            if (changeable.size() == 1) {
                changeTarget(gameData, targetSpell, changeable.getFirst(), sourcePermanentId);
                return;
            }
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChangeSpellTargetToSource(
                    targetSpell.getTargetableId(), sourcePermanentId));
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(),
                    changeable.stream().filter(id -> !gameData.playerIds.contains(id)).toList(),
                    changeable.stream().filter(gameData.playerIds::contains).toList(),
                    "Choose the target of " + targetSpell.getCard().getName() + " to change to "
                            + entry.getCard().getName() + ".");
            return;
        }

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " has no effect (", targetSpell.getCard(), " has no target that can be changed)."));
    }

    /** Applies the target chosen to be changed by a multi-target spell or ability. */
    public void completeChoice(GameData gameData, UUID chosenTargetId,
                               PermanentChoiceContext.ChangeSpellTargetToSource context) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, context.spellTargetableId());
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (targetSpell == null || sourcePermanent == null
                || !targetSpell.getDeclaredTargetIds().contains(chosenTargetId)
                || !targetRedirectionSupport.isValidNewTargetForSpell(
                        gameData, targetSpell, context.sourcePermanentId())) {
            return;
        }
        changeTarget(gameData, targetSpell, chosenTargetId, context.sourcePermanentId());
    }

    private UUID onlyTargetOf(StackEntry targetSpell) {
        return targetSpell.getTargetId() != null ? targetSpell.getTargetId() : targetSpell.getDeclaredTargetIds().getFirst();
    }

    private void changeTarget(GameData gameData, StackEntry targetSpell, UUID oldTargetId, UUID newTargetId) {
        if (targetSpell.getTargetId() != null) {
            targetSpell.setTargetId(newTargetId);
        } else {
            targetSpell.replaceTargetIdAt(targetSpell.getDeclaredTargetIds().indexOf(oldTargetId), newTargetId);
        }
        Permanent newTarget = gameQueryService.findPermanentById(gameData, newTargetId);
        if (newTarget != null) {
            gameLogService.append(gameData, GameLog.cardTextCard(targetSpell.getCard(), "'s target is changed to ", newTarget.getCard(), "."));
        }
    }
}
