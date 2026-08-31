package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectChosenOtherCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class SuspectChosenOtherCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SuspectChosenOtherCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = source(gameData, entry);
        if (source == null || !source.isSuspected()) {
            return;
        }

        List<UUID> eligibleIds = eligibleCreatureIds(gameData, entry);
        if (eligibleIds.isEmpty()) {
            return;
        }
        if (eligibleIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.SuspectChosenOtherCreature());
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligibleIds,
                    "Choose another creature to suspect.");
            return;
        }

        apply(gameData, entry, eligibleIds.getFirst());
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }

        Permanent source = source(gameData, entry);
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (source == null || !source.isSuspected()
                || chosen == null
                || !gameQueryService.isCreature(gameData, chosen)
                || chosen.getId().equals(source.getId())
                || !entry.getControllerId().equals(
                gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            throw new IllegalStateException("Choose another creature you control");
        }

        apply(gameData, entry, chosenPermanentId);
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of());
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<UUID> eligibleIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!permanent.getId().equals(sourcePermanentId)
                    && gameQueryService.isCreature(gameData, permanent)) {
                eligibleIds.add(permanent.getId());
            }
        }
        return eligibleIds;
    }

    private Permanent source(GameData gameData, StackEntry entry) {
        return entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
    }

    private void apply(GameData gameData, StackEntry entry, UUID chosenPermanentId) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        Permanent source = source(gameData, entry);
        if (chosen == null || source == null) {
            return;
        }

        if (!chosen.isSuspected()) {
            if (gameQueryService.cantBecomeSuspected(gameData, chosen)) {
                return;
            }
            chosen.setSuspected(true);
            gameLogService.append(gameData, GameLog.cardThen(chosen.getCard(), " is suspected."));
        }
        source.setSuspected(false);
        log.info("Game {} - {} suspects {} and is no longer suspected",
                gameData.id, source.getCard().getName(), chosen.getCard().getName());
    }
}
