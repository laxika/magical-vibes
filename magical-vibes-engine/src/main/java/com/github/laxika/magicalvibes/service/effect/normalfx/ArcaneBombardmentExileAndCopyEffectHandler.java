package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ArcaneBombardmentExileAndCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Arcane Bombardment's random graveyard exile and copy trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArcaneBombardmentExileAndCopyEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ArcaneBombardmentExileAndCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        List<Card> candidates = graveyard.stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();

        if (!candidates.isEmpty()) {
            Card exiled = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
            exileService.exileCard(gameData, controllerId, exiled, sourcePermanentId);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                    .card(exiled)
                    .text(" at random from their graveyard with Arcane Bombardment.")
                    .build());
        }

        List<Card> exiledSpells = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();
        List<UUID> copyIds = new ArrayList<>(exiledSpells.size());
        for (Card exiledSpell : exiledSpells) {
            Card copy = copySupport.createCopyCard(exiledSpell);
            exileService.exileCard(gameData, controllerId, copy);
            copyIds.add(copy.getId());
        }

        if (!copyIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.EyeOfTheStormCastChoice(controllerId, copyIds));
        }
        log.info("Game {} - Arcane Bombardment created {} spell copies for {}",
                gameData.id, copyIds.size(), gameData.playerIdToName.get(controllerId));
    }
}
