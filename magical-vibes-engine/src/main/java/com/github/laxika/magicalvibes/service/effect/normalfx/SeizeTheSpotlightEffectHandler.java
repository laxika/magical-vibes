package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SeizeTheSpotlightEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Seize the Spotlight's independent fame-or-fortune choices. */
@Component
@RequiredArgsConstructor
public class SeizeTheSpotlightEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenEffect TREASURE = CreateTokenEffect.ofTreasureToken(1);

    private final CreatureControlService creatureControlService;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeizeTheSpotlightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextModeChoice(gameData, apnapOpponents(gameData, entry.getControllerId()),
                entry.getControllerId(), List.of(), List.of(), entry.getCard().getName());
    }

    public void completeModeChoice(GameData gameData, String choice,
                                   ChoiceContext.SeizeTheSpotlightChoice context) {
        if (!ChoiceContext.SeizeTheSpotlightChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Seize the Spotlight choice: " + choice);
        }

        List<UUID> fame = new ArrayList<>(context.famePlayerIds());
        List<UUID> fortune = new ArrayList<>(context.fortunePlayerIds());
        if (ChoiceContext.SeizeTheSpotlightChoice.FAME.equals(choice)) {
            fame.add(context.choosingPlayerId());
        } else {
            fortune.add(context.choosingPlayerId());
        }

        beginNextModeChoice(gameData, context.remainingPlayerIds(), context.effectControllerId(),
                fame, fortune, context.sourceName());
    }

    public void completeCreatureChoice(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.SeizeTheSpotlightCreatureChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        List<UUID> chosenIds = new ArrayList<>(context.chosenPermanentIds());
        chosenIds.add(permanentId);
        beginNextCreatureChoice(gameData, context.controllerId(), context.remainingOpponentIds(),
                chosenIds, context.fortunePlayerIds(), context.sourceCardName());
    }

    private void beginNextModeChoice(GameData gameData, List<UUID> remainingPlayerIds,
                                     UUID controllerId, List<UUID> famePlayerIds,
                                     List<UUID> fortunePlayerIds, String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingPlayerIds);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (remaining.isEmpty()) {
            beginNextCreatureChoice(gameData, controllerId, famePlayerIds, List.of(),
                    fortunePlayerIds, sourceName);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.SeizeTheSpotlightChoice(
                        controllerId, choosingPlayerId, remaining, famePlayerIds,
                        fortunePlayerIds, sourceName),
                ChoiceContext.SeizeTheSpotlightChoice.OPTIONS,
                sourceName + " — choose fame or fortune."));
    }

    private void beginNextCreatureChoice(GameData gameData, UUID controllerId,
                                         List<UUID> remainingOpponentIds,
                                         List<UUID> chosenPermanentIds, List<UUID> fortunePlayerIds,
                                         String sourceName) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        List<UUID> chosen = new ArrayList<>(chosenPermanentIds);

        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, opponentId,
                    ignored -> true);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                chosen.add(candidates.getFirst());
                continue;
            }

            playerInputService.beginPermanentChoice(gameData, controllerId, candidates,
                    new PermanentChoiceContext.SeizeTheSpotlightCreatureChoice(
                            controllerId, opponentId, remaining, chosen, fortunePlayerIds, sourceName),
                    sourceName + " — choose a creature controlled by "
                            + gameData.playerIdToName.get(opponentId) + ".");
            return;
        }

        applyFame(gameData, controllerId, chosen, sourceName);
        insertFortuneEffects(gameData, controllerId, fortunePlayerIds.size());
    }

    private void applyFame(GameData gameData, UUID controllerId, List<UUID> chosenIds,
                           String sourceName) {
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.END_OF_TURN);
        for (UUID chosenId : chosenIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, chosenId);
            if (creature == null) {
                continue;
            }
            creatureControlService.applyControlEffect(gameData, controllerId, creature,
                    controlEffect, ControlDuration.END_OF_TURN.toEffectDuration(), null, sourceName);
            tapUntapSupport.untapPermanent(gameData, creature);
            grantKeywordEffectHandler.grantToPermanent(gameData, sourceName, controllerId, creature,
                    Set.of(Keyword.HASTE));
        }
    }

    private void insertFortuneEffects(GameData gameData, UUID controllerId, int fortuneCount) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Seize the Spotlight resolution is not resumable");
        }

        List<CardEffect> effects = new ArrayList<>();
        for (int i = 0; i < fortuneCount; i++) {
            effects.add(new DrawCardForPlayerEffect(controllerId));
            effects.add(new CreateTokenForPlayerEffect(controllerId, TREASURE));
        }
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, effects);
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
