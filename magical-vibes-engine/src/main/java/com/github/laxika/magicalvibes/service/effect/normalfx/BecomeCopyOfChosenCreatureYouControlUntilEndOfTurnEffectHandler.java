package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final BecomeCopyOfTargetCreatureUntilEndOfTurnEffectHandler targetCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Permanent> otherCreatures = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!permanent.getId().equals(sourcePermanentId)
                        && gameQueryService.isCreature(gameData, permanent)) {
                    otherCreatures.add(permanent);
                }
            }
        }

        if (otherCreatures.isEmpty()) {
            log.info("Game {} - {} controls no other creatures to copy", gameData.id, entry.getCard().getName());
            return;
        }

        if (otherCreatures.size() == 1) {
            applyCopy(gameData, entry, otherCreatures.getFirst().getId());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DeepfathomEchoCreatureChoice(
                        entry.getControllerId(), sourcePermanentId));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(),
                otherCreatures.stream().map(Permanent::getId).toList(),
                "Choose another creature you control to copy.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.DeepfathomEchoCreatureChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, chosenPermanentId))
                || !gameQueryService.isCreature(gameData, chosen)
                || chosenPermanentId.equals(context.sourcePermanentId())) {
            throw new IllegalStateException("Chosen permanent is not another creature you control");
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Deepfathom Echo resolution is no longer pending");
        }

        applyCopy(gameData, pendingEntry, chosenPermanentId);
    }

    private void applyCopy(GameData gameData, StackEntry entry, UUID chosenPermanentId) {
        UUID previousTargetId = entry.getTargetId();
        entry.setTargetIdForEffectResolution(chosenPermanentId);
        try {
            targetCopyHandler.resolve(gameData, entry, new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect());
        } finally {
            entry.restoreTargetIdAfterEffectResolution(previousTargetId);
        }
    }
}
