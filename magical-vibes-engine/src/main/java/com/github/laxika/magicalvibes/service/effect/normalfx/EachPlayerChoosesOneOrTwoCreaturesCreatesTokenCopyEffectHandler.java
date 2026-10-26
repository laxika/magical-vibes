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

    /**
     * Creates each player's copy, in APNAP order, as that player's own token creation event whose replacements
     * they order (CR 616.1), each starting once the previous player's tokens have entered (the creation may pause
     * for the order prompt or Jinnie Fay). Once every player's tokens are on the battlefield, each player who chose
     * a second creature puts +1/+1 counters equal to its power on every token they created this way, tokens a
     * replacement adds or substitutes (Queen Allenal's Soldier) included.
     */
    private void createCopiesAndCounters(GameData gameData, Map<UUID, List<UUID>> chosenByPlayer,
            Card sourceCard) {
        createCopyForPlayer(gameData, List.copyOf(chosenByPlayer.entrySet()), 0, new LinkedHashMap<>(),
                sourceCard);
    }

    private void createCopyForPlayer(GameData gameData, List<Map.Entry<UUID, List<UUID>>> choices, int index,
            Map<UUID, List<UUID>> tokenIdsByPlayer, Card sourceCard) {
        for (int i = index; i < choices.size(); i++) {
            UUID playerId = choices.get(i).getKey();
            Permanent copiedCreature = gameQueryService.findPermanentById(gameData, choices.get(i).getValue().getFirst());
            if (copiedCreature == null || !isControlledCreature(gameData, copiedCreature, playerId)) {
                continue;
            }
            int next = i + 1;
            tokenCopySupport.createTokenCopiesThen(gameData, new StackEntry(sourceCard, playerId),
                    List.of(copiedCreature.getCard()), playerId, TOKEN_PROFILE, null,
                    (game, tokens) -> {
                        tokenIdsByPlayer.put(playerId, tokens.stream().map(Permanent::getId).toList());
                        createCopyForPlayer(game, choices, next, tokenIdsByPlayer, sourceCard);
                    });
            return;
        }
        putCounters(gameData, choices, tokenIdsByPlayer, sourceCard);
    }

    private void putCounters(GameData gameData, List<Map.Entry<UUID, List<UUID>>> choices,
            Map<UUID, List<UUID>> tokenIdsByPlayer, Card sourceCard) {
        for (Map.Entry<UUID, List<UUID>> choice : choices) {
            List<UUID> tokenIds = tokenIdsByPlayer.get(choice.getKey());
            if (tokenIds == null || choice.getValue().size() < 2) {
                continue;
            }
            Permanent secondCreature = gameQueryService.findPermanentById(gameData, choice.getValue().get(1));
            if (secondCreature == null) {
                continue;
            }
            int counterCount = Math.max(0, gameQueryService.getEffectivePower(gameData, secondCreature));
            if (counterCount == 0) {
                continue;
            }
            StackEntry counterEntry = new StackEntry(sourceCard, choice.getKey());
            for (UUID tokenId : tokenIds) {
                Permanent token = gameQueryService.findPermanentById(gameData, tokenId);
                if (token != null) {
                    permanentCounterSupport.placeCounterOnPermanent(
                            gameData, counterEntry, token, CounterType.PLUS_ONE_PLUS_ONE, counterCount);
                }
            }
        }
    }

    private List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
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
}
