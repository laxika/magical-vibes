package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ZndrsplatsJudgmentActionEffect;
import com.github.laxika.magicalvibes.model.effect.ZndrsplatsJudgmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the per-player choices of Zndrsplt's Judgment. */
@Component
@RequiredArgsConstructor
public class ZndrsplatsJudgmentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ZndrsplatsJudgmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginModeChoice(gameData, apnapPlayers(gameData), List.of(), List.of(), List.of(),
                entry.getCard().getName());
    }

    public void completeModeChoice(GameData gameData, String choice,
                                   ChoiceContext.ZndrsplatsJudgmentChoice context) {
        if (!context.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Zndrsplt's Judgment choice: " + choice);
        }

        List<UUID> remaining = new ArrayList<>(context.remainingPlayerIds());
        List<UUID> chosen = new ArrayList<>(context.chosenPlayerIds());
        List<UUID> friends = new ArrayList<>(context.friendPlayerIds());
        List<UUID> foes = new ArrayList<>(context.foePlayerIds());
        UUID choosingPlayerId = context.choosingPlayerId();
        chosen.add(choosingPlayerId);
        if (ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND.equals(choice)) {
            friends.add(choosingPlayerId);
        } else {
            foes.add(choosingPlayerId);
        }

        beginModeChoice(gameData, remaining, chosen, friends, foes, context.sourceName());
    }

    public void completeCreatureChoice(GameData gameData, UUID creatureId,
                                       PermanentChoiceContext.ZndrsplatsJudgmentCreatureChoice context) {
        if (context.remainingSelections().isEmpty()) {
            finish(gameData, context.chosenSelections());
            return;
        }

        PermanentChoiceContext.ZndrsplatsJudgmentSelection selection =
                context.remainingSelections().getFirst();
        List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> remaining =
                new ArrayList<>(context.remainingSelections().subList(1, context.remainingSelections().size()));
        List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> chosen =
                new ArrayList<>(context.chosenSelections());
        Permanent permanent = gameQueryService.findPermanentById(gameData, creatureId);
        if (permanent != null
                && selection.playerId().equals(gameQueryService.findPermanentController(gameData, creatureId))
                && gameQueryService.isCreature(gameData, permanent)) {
            chosen.add(new PermanentChoiceContext.ZndrsplatsJudgmentSelection(
                    selection.playerId(), selection.friend(), creatureId));
        }

        beginCreatureChoice(gameData, remaining, chosen, context.sourceCardName());
    }

    private void beginModeChoice(GameData gameData, List<UUID> remainingPlayerIds,
                                 List<UUID> chosenPlayerIds, List<UUID> friendPlayerIds,
                                 List<UUID> foePlayerIds, String sourceName) {
        if (remainingPlayerIds.isEmpty()) {
            List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> selections = chosenPlayerIds.stream()
                    .map(playerId -> new PermanentChoiceContext.ZndrsplatsJudgmentSelection(
                            playerId, friendPlayerIds.contains(playerId), null))
                    .toList();
            beginCreatureChoice(gameData, selections, List.of(), sourceName);
            return;
        }

        UUID choosingPlayerId = remainingPlayerIds.getFirst();
        List<UUID> nextRemaining = remainingPlayerIds.size() == 1
                ? List.of()
                : List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()));
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.ZndrsplatsJudgmentChoice(
                        choosingPlayerId, nextRemaining, chosenPlayerIds,
                        friendPlayerIds, foePlayerIds, sourceName),
                ChoiceContext.ZndrsplatsJudgmentChoice.OPTIONS,
                sourceName + " - Choose friend or foe."));
    }

    private void beginCreatureChoice(GameData gameData,
                                     List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> remainingSelections,
                                     List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> chosenSelections,
                                     String sourceName) {
        if (remainingSelections.isEmpty()) {
            finish(gameData, chosenSelections);
            return;
        }

        PermanentChoiceContext.ZndrsplatsJudgmentSelection selection = remainingSelections.getFirst();
        List<UUID> creatureIds = gameData.playerBattlefields.getOrDefault(selection.playerId(), List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> nextRemaining = remainingSelections.size() == 1
                ? List.of()
                : List.copyOf(remainingSelections.subList(1, remainingSelections.size()));
        if (creatureIds.isEmpty()) {
            beginCreatureChoice(gameData, nextRemaining, chosenSelections, sourceName);
        } else if (creatureIds.size() == 1) {
            List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> nextChosen = new ArrayList<>(chosenSelections);
            nextChosen.add(new PermanentChoiceContext.ZndrsplatsJudgmentSelection(
                    selection.playerId(), selection.friend(), creatureIds.getFirst()));
            beginCreatureChoice(gameData, nextRemaining, nextChosen, sourceName);
        } else {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ZndrsplatsJudgmentCreatureChoice(
                            remainingSelections, chosenSelections, sourceName));
            playerInputService.beginPermanentChoice(gameData, selection.playerId(), creatureIds,
                    sourceName + " - Choose a creature you control.");
        }
    }

    private void finish(GameData gameData,
                        List<PermanentChoiceContext.ZndrsplatsJudgmentSelection> selections) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("Zndrsplt's Judgment resolution is not resumable");
        }

        List<CardEffect> actions = selections.stream()
                .filter(selection -> selection.creatureId() != null)
                .map(selection -> (CardEffect) new ZndrsplatsJudgmentActionEffect(
                        selection.playerId(), selection.creatureId(), selection.friend()))
                .toList();
        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, actions);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
            rotated.addAll(players.subList(0, activeIndex));
            return rotated;
        }
        return players;
    }
}
