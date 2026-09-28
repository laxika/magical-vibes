package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/** Resolves Wildfire Devils' random-player graveyard copy ability. */
@Component
@RequiredArgsConstructor
public class RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffectHandler
        implements NormalEffectHandlerBean {

    private static final ExileTargetCardFromGraveyardAndMayCastCopyEffect COPY_EFFECT =
            new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                    null, GraveyardSearchScope.ALL_GRAVEYARDS);

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            resolveChosenCard(gameData, entry, entry.getTargetId());
            return;
        }

        List<UUID> players = new ArrayList<>(gameData.playerIds);
        if (players.isEmpty()) {
            return;
        }

        UUID selectedPlayerId = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(selectedPlayerId, List.of());
        List<Integer> validIndices = IntStream.range(0, graveyard.size())
                .filter(index -> isInstantOrSorcery(graveyard.get(index)))
                .boxed()
                .toList();
        if (validIndices.isEmpty()) {
            return;
        }

        if (validIndices.size() == 1) {
            resolveChosenCard(gameData, entry, graveyard.get(validIndices.getFirst()));
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        selectedPlayerId, validIndices, GraveyardChoiceDestination.RANDOM_PLAYER_GRAVEYARD_COPY,
                        entry.getCard().getName() + " — Choose an instant or sorcery card from your graveyard to exile.")
                .mandatory(true)
                .build());
    }

    private void resolveChosenCard(GameData gameData, StackEntry entry, Card chosenCard) {
        resolveChosenCard(gameData, entry, chosenCard.getId());
    }

    private void resolveChosenCard(GameData gameData, StackEntry entry, UUID cardId) {
        Card chosenCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (chosenCard == null || !isInstantOrSorcery(chosenCard)) {
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (graveyardOwnerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
        exileService.exileCard(gameData, graveyardOwnerId, chosenCard);
        gameLogService.append(gameData, GameLog.isExiled(chosenCard));

        Card copy = copySupport.createCopyCard(chosenCard);
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(COPY_EFFECT),
                "Cast the copy of " + copy.getName() + "?",
                copy.getId(),
                null,
                entry.getSourcePermanentId()));
    }

    private boolean isInstantOrSorcery(Card card) {
        return card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY);
    }
}
