package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsChooseOneMayPlayUntilNextEndStepEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect exileEffect =
                (ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect) effect;
        if (exileEffect.count() <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID libraryOwnerId = subjectResolutionService.player(gameData, entry, exileEffect, exileEffect.player());
        if (libraryOwnerId == null || !gameData.orderedPlayerIds.contains(libraryOwnerId)) {
            return;
        }
        UUID permissionPlayerId = exileEffect.player() instanceof PlayerSubject.You
                ? controllerId : libraryOwnerId;
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<UUID> exiledIds = new ArrayList<>();
        String libraryOwnerName = gameData.playerIdToName.get(libraryOwnerId);
        for (int i = 0; i < exileEffect.count() && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, libraryOwnerId, topCard);
            exiledIds.add(topCard.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(libraryOwnerName + " exiles ").card(topCard)
                    .text(" from the top of their library.").build());
        }

        if (!exiledIds.isEmpty() && !exileEffect.chooseOnExile()) {
            for (UUID exiledId : exiledIds) {
                exileSupport.grantPlayUntilOwnersNextEndStep(gameData, exiledId, permissionPlayerId);
                if (exileEffect.withoutPayingManaCost()) {
                    gameData.setExilePlayWithoutPayingManaCost(exiledId, true);
                }
            }
            gameData.registerExilePlayPermissionGroup(UUID.randomUUID(), 1, exiledIds);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(permissionPlayerId)
                            + " may play one of those cards until their next end step."));
        } else if (!exiledIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ExiledCardMayPlayChoice(
                            permissionPlayerId, exiledIds, Duration.UNTIL_YOUR_NEXT_END_STEP,
                            false, exileEffect.withoutPayingManaCost()));
            log.info("Game {} - {} chooses one card among {} exiled cards to play until next end step",
                    gameData.id, entry.getCard().getName(), exiledIds.size());
        }
    }
}
