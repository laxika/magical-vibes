package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesMasterOfCeremoniesEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Master of Ceremonies' per-opponent mode choices and rewards. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesMasterOfCeremoniesEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesMasterOfCeremoniesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (EachOpponentChoosesMasterOfCeremoniesEffect) effect;
        UUID controllerId = choiceEffect.abilityControllerId() != null
                ? choiceEffect.abilityControllerId() : entry.getControllerId();
        List<UUID> opponents = choiceEffect.remainingOpponentIds() == null
                ? apnapOpponents(gameData, controllerId)
                : new ArrayList<>(choiceEffect.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));

        beginNextChoice(gameData, entry.getCard().getName(), controllerId, opponents,
                choiceEffect.moneyPlayerIds(), choiceEffect.friendsPlayerIds(), choiceEffect.secretsPlayerIds());
    }

    public void completeChoice(GameData gameData, String choice, UUID choosingPlayerId,
                               ChoiceContext.MasterOfCeremoniesChoice context) {
        if (!ChoiceContext.MasterOfCeremoniesChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Master of Ceremonies choice: " + choice);
        }

        List<UUID> moneyPlayers = new ArrayList<>(context.moneyPlayerIds());
        List<UUID> friendsPlayers = new ArrayList<>(context.friendsPlayerIds());
        List<UUID> secretsPlayers = new ArrayList<>(context.secretsPlayerIds());
        switch (choice) {
            case ChoiceContext.MasterOfCeremoniesChoice.MONEY -> moneyPlayers.add(choosingPlayerId);
            case ChoiceContext.MasterOfCeremoniesChoice.FRIENDS -> friendsPlayers.add(choosingPlayerId);
            case ChoiceContext.MasterOfCeremoniesChoice.SECRETS -> secretsPlayers.add(choosingPlayerId);
            default -> throw new IllegalArgumentException("Invalid Master of Ceremonies choice: " + choice);
        }

        beginNextChoice(gameData, context.sourceName(), context.effectControllerId(),
                context.remainingOpponentIds(), moneyPlayers, friendsPlayers, secretsPlayers);
    }

    private void beginNextChoice(GameData gameData, String sourceName, UUID controllerId,
                                 List<UUID> remainingOpponentIds, List<UUID> moneyPlayers,
                                 List<UUID> friendsPlayers, List<UUID> secretsPlayers) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (remaining.isEmpty()) {
            applyRewards(gameData, controllerId, moneyPlayers, friendsPlayers, secretsPlayers);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.MasterOfCeremoniesChoice(
                        controllerId, remaining, moneyPlayers, friendsPlayers, secretsPlayers, sourceName),
                ChoiceContext.MasterOfCeremoniesChoice.OPTIONS,
                sourceName + " — choose money, friends, or secrets."));
    }

    private void applyRewards(GameData gameData, UUID controllerId,
                              List<UUID> moneyPlayers, List<UUID> friendsPlayers,
                              List<UUID> secretsPlayers) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Master of Ceremonies resolution is not resumable");
        }

        CreateTokenEffect treasure = CreateTokenEffect.ofTreasureToken(1);
        createTokens(gameData, entry, controllerId, treasure, moneyPlayers.size());
        for (UUID playerId : moneyPlayers) {
            createTokens(gameData, entry, playerId, treasure, 1);
        }

        CreateTokenEffect citizen = new CreateTokenEffect(
                1, "Citizen", 1, 1, CardColor.GREEN,
                java.util.Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN));
        createTokens(gameData, entry, controllerId, citizen, friendsPlayers.size());
        for (UUID playerId : friendsPlayers) {
            createTokens(gameData, entry, playerId, citizen, 1);
        }

        for (int i = 0; i < secretsPlayers.size(); i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        for (UUID playerId : secretsPlayers) {
            drawService.resolveDrawCard(gameData, playerId);
        }
    }

    private void createTokens(GameData gameData, StackEntry entry, UUID playerId,
                              CreateTokenEffect token, int amount) {
        if (amount > 0 && gameData.playerIds.contains(playerId)) {
            createTokenEffectHandler.resolveForController(gameData, entry, token.withAmount(amount), playerId);
        }
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return new ArrayList<>(ordered.stream().filter(id -> !id.equals(controllerId)).toList());
    }
}
