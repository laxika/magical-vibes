package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Human—Time Lord Meta-Crisis's ordered one-or-two-creature choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE =
            CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                    List.of(), java.util.Set.of(), null, null, Map.of());

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        chooseForPlayer(gameData, apnapPlayers(gameData), 0, new LinkedHashMap<>(),
                entry.getCard(), entry.getControllerId());
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyChoice context) {
        UUID playerId = context.playerIds().get(context.playerIndex());
        List<UUID> validCreatureIds = creatureIds(gameData, playerId);
        if (permanentIds.size() < 1 || permanentIds.size() > 2
                || permanentIds.stream().anyMatch(id -> !validCreatureIds.contains(id))) {
            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, validCreatureIds, Math.min(2, validCreatureIds.size()), context,
                    context.sourceCard().getName() + " — choose one or two creatures you control.");
            return;
        }

        Map<UUID, List<UUID>> choices = new LinkedHashMap<>(context.chosenByPlayer());
        choices.put(playerId, List.copyOf(permanentIds));
        chooseForPlayer(gameData, context.playerIds(), context.playerIndex() + 1, choices,
                context.sourceCard(), context.controllerId());
    }

    private void chooseForPlayer(GameData gameData, List<UUID> playerIds, int playerIndex,
            Map<UUID, List<UUID>> chosenByPlayer, Card sourceCard, UUID controllerId) {
        for (int index = playerIndex; index < playerIds.size(); index++) {
            UUID playerId = playerIds.get(index);
            List<UUID> creatureIds = creatureIds(gameData, playerId);
            if (creatureIds.isEmpty()) {
                continue;
            }
            if (creatureIds.size() == 1) {
                chosenByPlayer.put(playerId, List.of(creatureIds.getFirst()));
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, creatureIds, 2,
                    new MultiPermanentChoiceContext.EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyChoice(
                            playerIds, index, chosenByPlayer, sourceCard, controllerId),
                    sourceCard.getName() + " — choose one or two creatures you control. "
                            + "The first chosen creature is copied.");
            return;
        }

        createCopiesAndCounters(gameData, chosenByPlayer, sourceCard);
    }

    private void createCopiesAndCounters(GameData gameData, Map<UUID, List<UUID>> chosenByPlayer,
            Card sourceCard) {
        List<CreatedCopy> createdCopies = new ArrayList<>();
        for (Map.Entry<UUID, List<UUID>> choice : chosenByPlayer.entrySet()) {
            List<UUID> selectedIds = choice.getValue();
            Permanent copiedCreature = gameQueryService.findPermanentById(gameData, selectedIds.getFirst());
            if (copiedCreature == null || !isControlledCreature(gameData, copiedCreature, choice.getKey())) {
                continue;
            }

            StackEntry copyEntry = new StackEntry(sourceCard, choice.getKey());
            int expectedCopyCount = gameQueryService.getTokenCreationAmount(
                    gameData, choice.getKey(), 1, copiedCreature.getCard().getSubtypes(),
                    gameQueryService.isCreature(gameData, copiedCreature));
            List<UUID> createdIds = tokenCopySupport.createTokenCopies(
                    gameData, copyEntry, List.of(copiedCreature.getCard()), null,
                    choice.getKey(), TOKEN_PROFILE);
            int copyCount = Math.min(expectedCopyCount, createdIds.size());
            createdCopies.add(new CreatedCopy(choice.getKey(), selectedIds,
                    List.copyOf(createdIds.subList(0, copyCount)), copyEntry));
        }

        for (CreatedCopy createdCopy : createdCopies) {
            if (createdCopy.selectedIds().size() < 2) {
                continue;
            }
            Permanent secondCreature = gameQueryService.findPermanentById(
                    gameData, createdCopy.selectedIds().get(1));
            if (secondCreature == null) {
                continue;
            }
            int counterCount = Math.max(0, gameQueryService.getEffectivePower(gameData, secondCreature));
            for (UUID tokenId : createdCopy.tokenIds()) {
                Permanent token = gameQueryService.findPermanentById(gameData, tokenId);
                if (token != null && counterCount > 0) {
                    permanentCounterSupport.placeCounterOnPermanent(
                            gameData, createdCopy.copyEntry(), token,
                            CounterType.PLUS_ONE_PLUS_ONE, counterCount);
                }
            }
        }
    }

    private List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private boolean isControlledCreature(GameData gameData, Permanent permanent, UUID playerId) {
        return playerId.equals(gameQueryService.findPermanentController(gameData, permanent.getId()))
                && gameQueryService.isCreature(gameData, permanent);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
        rotated.addAll(players.subList(0, activeIndex));
        return rotated;
    }

    private record CreatedCopy(UUID controllerId, List<UUID> selectedIds,
                               List<UUID> tokenIds, StackEntry copyEntry) {
    }
}
