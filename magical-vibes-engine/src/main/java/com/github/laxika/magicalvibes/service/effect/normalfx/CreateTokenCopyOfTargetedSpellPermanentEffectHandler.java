package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetedSpellPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetedSpellPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetedSpellPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfTargetedSpellPermanentEffect copyEffect =
                (CreateTokenCopyOfTargetedSpellPermanentEffect) effect;
        if (copyEffect.spellSnapshot() == null) return;

        List<UUID> validTargetIds = validTargetIds(gameData, entry, copyEffect.spellSnapshot());
        if (validTargetIds.isEmpty()) return;

        UUID chosenId = entry.getTargetId();
        if (chosenId == null || !validTargetIds.contains(chosenId)) {
            if (validTargetIds.size() > 1) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.CopyPermanentTargetedBySpell());
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validTargetIds,
                        entry.getCard().getName() + " — choose a permanent to copy.");
                return;
            }
            chosenId = validTargetIds.getFirst();
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        StackEntry copyEntry = new StackEntry(entry.getCard(), entry.getControllerId());
        copyEntry.setTargetId(chosenId);
        tokenCopyHandler.resolve(gameData, copyEntry, new CreateTokenCopyOfTargetPermanentEffect());
    }

    private List<UUID> validTargetIds(GameData gameData, StackEntry entry, StackEntry spellSnapshot) {
        List<UUID> targetIds = permanentTargetIds(gameData, spellSnapshot);
        List<UUID> validIds = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
            if (permanent != null
                    && !targetId.equals(entry.getSourcePermanentId())
                    && entry.getControllerId().equals(gameData.findControllerOf(permanent))) {
                validIds.add(targetId);
            }
        }
        return validIds;
    }

    private List<UUID> permanentTargetIds(GameData gameData, StackEntry spellSnapshot) {
        List<UUID> targetIds = new ArrayList<>(spellSnapshot.getDeclaredTargetIds());
        UUID primaryTargetId = spellSnapshot.getTargetId();
        if (primaryTargetId != null && spellSnapshot.getTargetZone() == null
                && (targetIds.isEmpty() || spellSnapshot.isPrimaryTargetStoredSeparately())) {
            if (spellSnapshot.isPrimaryTargetStoredSeparately()) {
                targetIds.addFirst(primaryTargetId);
            } else {
                targetIds.add(primaryTargetId);
            }
        }
        return targetIds.stream()
                .filter(targetId -> !gameData.playerIds.contains(targetId))
                .distinct()
                .toList();
    }
}
