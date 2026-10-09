package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Keeps creature order fixed while positioning applies, and obtains new creatures' positions. */
@Component
@RequiredArgsConstructor
public class CreaturePositioningSupport {
    private final GameQueryService gameQueryService;
    @Lazy private final InteractionHandlerRegistry interactionHandlerRegistry;

    public boolean beginEntryChoice(GameData gameData, BattlefieldEntryRequest request) {
        Permanent entering = request.permanent();
        if (entering.getChosenCreaturePosition() != null
                || !gameQueryService.isCreature(gameData, entering)) return false;
        List<Permanent> creatures = creatureLine(gameData, request.controllerId(), entering.getId());
        boolean positioning = creatures.stream().anyMatch(p -> hasPositioning(gameData, p));
        positioning |= !entering.isFaceDown()
                && !gameQueryService.hasLostPrintedAbilitiesAsEntering(gameData, request.controllerId(), entering)
                && entering.getCard().hasKeyword(Keyword.POSITIONING);
        if (!positioning || creatures.isEmpty()) return false;
        begin(gameData, request.controllerId(), entering, creatures, request);
        return true;
    }

    public void onControlChange(GameData gameData, UUID controllerId, Permanent permanent) {
        if (!gameQueryService.isCreature(gameData, permanent)) return;
        List<Permanent> creatures = creatureLine(gameData, controllerId, permanent.getId());
        if (creatures.isEmpty() || (!hasPositioning(gameData, permanent)
                && creatures.stream().noneMatch(p -> hasPositioning(gameData, p)))) return;
        permanent.setChosenCreaturePosition(null);
        permanent.setCreaturePositionPending(true);
        beginNextControlChoice(gameData);
    }

    public void beginNextControlChoice(GameData gameData) {
        if (gameData.interaction.isAwaitingInput()) return;
        for (var battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent permanent : battlefield.getValue()) {
                if (!permanent.isCreaturePositionPending()) continue;
                List<Permanent> creatures = creatureLine(gameData, battlefield.getKey(), permanent.getId());
                if (creatures.isEmpty()) {
                    permanent.setCreaturePositionPending(false);
                    continue;
                }
                begin(gameData, battlefield.getKey(), permanent, creatures, null);
                return;
            }
        }
    }

    public BattlefieldEntryRequest completeChoice(GameData gameData, ChoiceContext.CreaturePositionChoice choice,
                                                  String answer) {
        int position = choice.options().indexOf(answer);
        if (position < 0) throw new IllegalArgumentException("Invalid creature position");
        Permanent permanent = choice.request() == null
                ? gameQueryService.findPermanentById(gameData, choice.permanentId()) : choice.request().permanent();
        if (permanent == null) throw new IllegalStateException("The creature is no longer on the battlefield");
        gameData.interaction.clearAwaitingInput();
        permanent.setChosenCreaturePosition(position);
        permanent.setCreaturePositionPending(false);
        if (choice.request() == null) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            applyChosenPosition(gameData, controllerId, permanent);
            beginNextControlChoice(gameData);
        }
        return choice.request();
    }

    public void applyChosenPosition(GameData gameData, UUID controllerId, Permanent permanent) {
        if (permanent.getChosenCreaturePosition() == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || !battlefield.remove(permanent)) return;
        int slot = permanent.getChosenCreaturePosition();
        int creatures = 0;
        int insertion = battlefield.size();
        for (int index = 0; index < battlefield.size(); index++) {
            if (!gameQueryService.isCreature(gameData, battlefield.get(index))) continue;
            if (creatures++ == slot) {
                insertion = index;
                break;
            }
        }
        battlefield.add(insertion, permanent);
    }

    private boolean hasPositioning(GameData gameData, Permanent permanent) {
        return gameQueryService.isCreature(gameData, permanent)
                && gameQueryService.hasKeyword(gameData, permanent, Keyword.POSITIONING);
    }

    private List<Permanent> creatureLine(GameData gameData, UUID controllerId, UUID excludedId) {
        List<Permanent> creatures = new ArrayList<>(gameData.playerBattlefields.getOrDefault(controllerId, List.of())
                .stream().filter(p -> !p.getId().equals(excludedId) && gameQueryService.isCreature(gameData, p)).toList());
        var batch = gameData.pendingBattlefieldEntryBatch;
        if (batch != null) {
            for (var candidate : batch.ready()) {
                Permanent prepared = candidate.preparedPermanent();
                if (!candidate.controllerId().equals(controllerId) || prepared == null
                        || prepared.getId().equals(excludedId) || !gameQueryService.isCreature(gameData, prepared)) continue;
                int position = prepared.getChosenCreaturePosition() == null ? creatures.size()
                        : Math.min(prepared.getChosenCreaturePosition(), creatures.size());
                creatures.add(position, prepared);
            }
        }
        return creatures;
    }

    private void begin(GameData gameData, UUID controllerId, Permanent permanent, List<Permanent> creatures,
                       BattlefieldEntryRequest request) {
        List<String> options = new ArrayList<>();
        options.add("Left of " + label(creatures, 0));
        for (int i = 1; i < creatures.size(); i++) {
            options.add("Between " + label(creatures, i - 1) + " and " + label(creatures, i));
        }
        options.add("Right of " + label(creatures, creatures.size() - 1));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(controllerId, null, null,
                new ChoiceContext.CreaturePositionChoice(request, permanent.getId(), options), options,
                "Choose a position for " + permanent.getCard().getName() + "."));
    }

    private String label(List<Permanent> creatures, int index) {
        return creatures.get(index).getCard().getName() + " (" + (index + 1) + ")";
    }
}
