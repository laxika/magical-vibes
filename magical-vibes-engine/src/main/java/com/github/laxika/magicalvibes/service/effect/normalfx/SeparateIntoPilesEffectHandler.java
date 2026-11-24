package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingPileStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link SeparateIntoPilesEffect}: for each group its controller separates the objects into two piles (a
 * multi-permanent or multi-graveyard choice of Pile 1, the rest forming Pile 2; either may be empty, CR 700.3d), then
 * the group's chooser picks one (Yes = Pile 1). Nothing moves (CR 700.3c). The step pauses at its end on these
 * choices, carried by {@link PendingPileStep}, and records {@link StepKind#CHOOSE} once the last pile is chosen: the
 * chosen piles' permanents and their controllers, or cards, their owners and the stay each was chosen in. The log and
 * prompts name a face-down permanent only as its public description (CR 708.2).
 */
@Component
@RequiredArgsConstructor
public class SeparateIntoPilesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeparateIntoPilesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var separate = (SeparateIntoPilesEffect) effect;
        UUID cardChooserId = separate.cards() == null ? null
                : subjectResolutionService.player(gameData, entry, effect, separate.chooser());
        entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
        List<PendingPileStep.Group> groups = new ArrayList<>();
        if (separate.permanents() != null) {
            var set = (PermanentSubject.ControlledBy) separate.permanents();
            List<Permanent> separated = subjectResolutionService.permanents(gameData, entry, effect, set);
            for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, set.players())) {
                List<UUID> theirs = separated.stream()
                        .map(Permanent::getId)
                        .filter(id -> playerId.equals(gameQueryService.findPermanentController(gameData, id)))
                        .toList();
                if (theirs.isEmpty()) {
                    logNothingToSeparate(gameData, playerId, separate);
                } else {
                    groups.add(new PendingPileStep.Group(playerId, theirs));
                }
            }
        } else {
            List<UUID> cardIds = subjectResolutionService.cards(gameData, entry, effect, separate.cards()).stream()
                    .map(Card::getId)
                    .toList();
            if (cardIds.isEmpty()) {
                logNothingToSeparate(gameData, entry.getControllerId(), separate);
            } else if (cardChooserId != null) {
                groups.add(new PendingPileStep.Group(cardChooserId, cardIds));
            }
        }
        if (groups.isEmpty()) {
            return;
        }
        gameData.recordPileGroupingOrGuess(entry);
        askSeparation(gameData, entry, new PendingPileStep(separate, entry.getControllerId(), groups.getFirst(),
                groups.subList(1, groups.size()), List.of(), List.of(), List.of()));
    }

    /** The separator has chosen Pile 1 among the group; the rest is Pile 2. The chooser is asked for one. */
    public void completeSeparation(GameData gameData, List<UUID> pile1Selection) {
        PendingPileStep state = gameData.pollPendingInteraction(PendingPileStep.class);
        Set<UUID> selected = new HashSet<>(pile1Selection);
        List<UUID> pile1 = state.group().objectIds().stream().filter(selected::contains).toList();
        List<UUID> pile2 = state.group().objectIds().stream().filter(id -> !selected.contains(id)).toList();
        gameData.queueInteraction(state.separated(pile1, pile2));

        var log = GameLog.builder().text(gameData.playerIdToName.get(state.separatorId()) + " separates "
                + state.effect().label() + " into two piles. Pile 1: ");
        appendPile(gameData, state, log, pile1);
        log.text(". Pile 2: ");
        appendPile(gameData, state, log, pile2);
        gameLogService.append(gameData, log.text(".").build());

        String prompt = "Choose a pile. Yes = Pile 1 (" + describePile(gameData, state, pile1) + "), No = Pile 2 ("
                + describePile(gameData, state, pile2) + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, state.group().chooserId(), List.of(),
                prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * The chooser has chosen a pile ({@code pile1}: Pile 1). The next group is separated, or the chosen piles are the
     * step's result.
     */
    public void completePileChoice(GameData gameData, boolean pile1) {
        PendingPileStep state = gameData.pollPendingInteraction(PendingPileStep.class);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(state.group().chooserId())
                + " chooses " + (pile1 ? "Pile 1" : "Pile 2") + "."));
        List<UUID> chosen = new ArrayList<>(state.chosenBefore());
        chosen.addAll(pile1 ? state.pile1Ids() : state.pile2Ids());
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (!state.laterGroups().isEmpty()) {
            askSeparation(gameData, entry, new PendingPileStep(state.effect(), state.separatorId(),
                    state.laterGroups().getFirst(), state.laterGroups().subList(1, state.laterGroups().size()),
                    chosen, List.of(), List.of()));
            return;
        }
        if (entry != null) {
            record(gameData, entry, state.effect(), chosen);
        }
    }

    private void askSeparation(GameData gameData, StackEntry entry, PendingPileStep state) {
        gameData.queueInteraction(state);
        String label = state.effect().label();
        String whose = state.effect().permanents() != null
                ? " " + gameData.playerIdToName.get(state.group().chooserId()) + " controls" : "";
        String prompt = FaceDownVisibility.sourceName(gameData, entry) + " — separate the " + label + whose
                + " into two piles. Select " + label + " for Pile 1 (unselected form Pile 2).";
        List<UUID> ids = state.group().objectIds();
        if (state.effect().permanents() != null) {
            playerInputService.beginMultiPermanentChoice(gameData, state.separatorId(), ids, ids.size(), prompt);
            return;
        }
        List<Card> cards = ids.stream()
                .map(id -> gameQueryService.findCardInGraveyardById(gameData, id))
                .filter(card -> card != null)
                .toList();
        playerInputService.beginMultiGraveyardChoice(gameData, state.separatorId(), cards, cards.size(), prompt);
    }

    private void record(GameData gameData, StackEntry entry, SeparateIntoPilesEffect separate, List<UUID> chosen) {
        List<UUID> objectIds = new ArrayList<>();
        List<UUID> playerIds = new ArrayList<>();
        List<MovedCard> moved = new ArrayList<>();
        for (UUID id : chosen) {
            UUID playerId;
            if (separate.permanents() != null) {
                if (gameQueryService.findPermanentById(gameData, id) == null) {
                    continue;
                }
                playerId = gameQueryService.findPermanentController(gameData, id);
            } else {
                if (gameQueryService.findCardInGraveyardById(gameData, id) == null) {
                    continue;
                }
                playerId = gameQueryService.findGraveyardOwnerById(gameData, id);
                moved.add(gameData.boundToStay(new MovedCard(id, id, Zone.GRAVEYARD, playerId)));
            }
            objectIds.add(id);
            if (playerId != null && !playerIds.contains(playerId)) {
                playerIds.add(playerId);
            }
        }
        if (!objectIds.isEmpty()) {
            entry.recordStep(StepKind.CHOOSE, moved.isEmpty() ? StepOutcome.ofObjects(objectIds, playerIds)
                    : StepOutcome.ofObjects(objectIds, playerIds, moved));
        }
    }

    private void logNothingToSeparate(GameData gameData, UUID playerId, SeparateIntoPilesEffect separate) {
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " has no "
                + separate.label() + " to separate."));
    }

    private void appendPile(GameData gameData, PendingPileStep state, GameLog.Builder log, List<UUID> pile) {
        if (pile.isEmpty()) {
            log.text("empty");
            return;
        }
        boolean first = true;
        for (UUID id : pile) {
            if (!first) {
                log.text(", ");
            }
            first = false;
            if (state.effect().permanents() != null) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                log.text(permanent == null ? "a permanent" : FaceDownVisibility.publicName(gameData, permanent));
            } else {
                Card card = gameQueryService.findCardInGraveyardById(gameData, id);
                if (card == null) {
                    log.text("a card");
                } else {
                    log.card(card);
                }
            }
        }
    }

    private String describePile(GameData gameData, PendingPileStep state, List<UUID> pile) {
        if (pile.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID id : pile) {
            if (state.effect().permanents() != null) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                if (permanent != null) {
                    names.add(FaceDownVisibility.publicName(gameData, permanent));
                }
            } else {
                Card card = gameQueryService.findCardInGraveyardById(gameData, id);
                if (card != null) {
                    names.add(card.getName());
                }
            }
        }
        return String.join(", ", names);
    }
}
