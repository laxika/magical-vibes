package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MasterOfCeremoniesEffect;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Master of Ceremonies' per-opponent choice and queues its results. */
@Component
@RequiredArgsConstructor
public class MasterOfCeremoniesEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenEffect TREASURE = CreateTokenEffect.ofTreasureToken(1);
    private static final CreateTokenEffect CITIZEN = new CreateTokenEffect(
            1, "Citizen", 1, 1, CardColor.GREEN,
            java.util.Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN));

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MasterOfCeremoniesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextChoice(gameData, apnapOpponents(gameData, entry.getControllerId()),
                entry.getControllerId(), List.of(), List.of(), List.of(), entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, String choice,
                               ChoiceContext.MasterOfCeremoniesChoice context) {
        if (!ChoiceContext.MasterOfCeremoniesChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Master of Ceremonies choice: " + choice);
        }

        List<UUID> money = new ArrayList<>(context.moneyPlayerIds());
        List<UUID> friends = new ArrayList<>(context.friendsPlayerIds());
        List<UUID> secrets = new ArrayList<>(context.secretsPlayerIds());
        switch (choice) {
            case ChoiceContext.MasterOfCeremoniesChoice.MONEY -> money.add(context.choosingPlayerId());
            case ChoiceContext.MasterOfCeremoniesChoice.FRIENDS -> friends.add(context.choosingPlayerId());
            case ChoiceContext.MasterOfCeremoniesChoice.SECRETS -> secrets.add(context.choosingPlayerId());
            default -> throw new IllegalArgumentException("Invalid Master of Ceremonies choice: " + choice);
        }

        beginNextChoice(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                money, friends, secrets, context.sourceName());
    }

    private void beginNextChoice(GameData gameData, List<UUID> remainingPlayerIds,
                                 UUID controllerId, List<UUID> moneyPlayerIds,
                                 List<UUID> friendsPlayerIds, List<UUID> secretsPlayerIds,
                                 String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (remaining.isEmpty()) {
            finish(gameData, controllerId, moneyPlayerIds, friendsPlayerIds, secretsPlayerIds);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.MasterOfCeremoniesChoice(
                        controllerId, choosingPlayerId, remaining, moneyPlayerIds,
                        friendsPlayerIds, secretsPlayerIds, sourceName),
                ChoiceContext.MasterOfCeremoniesChoice.OPTIONS,
                sourceName + " — choose money, friends, or secrets."));
    }

    private void finish(GameData gameData, UUID controllerId, List<UUID> moneyPlayerIds,
                        List<UUID> friendsPlayerIds, List<UUID> secretsPlayerIds) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Master of Ceremonies resolution is not resumable");
        }

        List<CardEffect> results = new ArrayList<>();
        for (UUID playerId : moneyPlayerIds) {
            results.add(new CreateTokenForPlayerEffect(controllerId, TREASURE));
            results.add(new CreateTokenForPlayerEffect(playerId, TREASURE));
        }
        for (UUID playerId : friendsPlayerIds) {
            results.add(new CreateTokenForPlayerEffect(controllerId, CITIZEN));
            results.add(new CreateTokenForPlayerEffect(playerId, CITIZEN));
        }
        for (UUID playerId : secretsPlayerIds) {
            results.add(new DrawCardForPlayerEffect(controllerId));
            results.add(new DrawCardForPlayerEffect(playerId));
        }
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, results);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)
                && gameData.playerIds.contains(activePlayerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId) && !playerId.equals(activePlayerId)
                    && gameData.playerIds.contains(playerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
