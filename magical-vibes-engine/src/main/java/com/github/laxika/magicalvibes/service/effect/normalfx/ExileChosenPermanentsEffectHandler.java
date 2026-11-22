package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileChosenPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileChosenPermanentsEffect.Choice;
import com.github.laxika.magicalvibes.model.effect.ExileChosenPermanentsEffect.SourceLink;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Asks for the permanents choice by choice, a choice with one candidate answering itself, then exiles them together
 * and records the step.
 */
@Component
@RequiredArgsConstructor
public class ExileChosenPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileChosenPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        chooseNext(gameData, entry, (ExileChosenPermanentsEffect) effect, List.of());
    }

    /** The answer to one choice. */
    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.ExileChosenPermanents context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }
        if (!options(gameData, entry, context.effect(), context.chosen()).contains(permanentId)) {
            throw new IllegalStateException("Chosen permanent is no longer a matching permanent you control");
        }
        List<UUID> chosen = new ArrayList<>(context.chosen());
        chosen.add(permanentId);
        chooseNext(gameData, entry, context.effect(), chosen);
    }

    private void chooseNext(GameData gameData, StackEntry entry, ExileChosenPermanentsEffect exile, List<UUID> chosen) {
        UUID sourceId = exile.link() == SourceLink.NONE ? null : sourcePermanentId(gameData, entry);
        if (exile.link() != SourceLink.NONE && (sourceId == null || exile.link() == SourceLink.UNTIL_SOURCE_LEAVES
                && gameQueryService.findPermanentById(gameData, sourceId) == null)) {
            return;
        }
        if (chosen.size() == exile.choices().size()) {
            exileTogether(gameData, entry, exile, chosen, sourceId);
            return;
        }
        List<UUID> options = options(gameData, entry, exile, chosen);
        if (options.isEmpty()) {
            return;
        }
        if (options.size() == 1) {
            List<UUID> more = new ArrayList<>(chosen);
            more.add(options.getFirst());
            chooseNext(gameData, entry, exile, more);
            return;
        }
        var context = new PermanentChoiceContext.ExileChosenPermanents(exile, chosen);
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), options, context,
                entry.getCard().getName() + " — choose a " + exile.choices().get(chosen.size()).label() + " to exile.");
    }

    /** The permanents the next choice may name that leave one for each choice after it. */
    private List<UUID> options(GameData gameData, StackEntry entry, ExileChosenPermanentsEffect exile,
                               List<UUID> chosen) {
        if (chosen.size() >= exile.choices().size()) {
            return List.of();
        }
        Choice choice = exile.choices().get(chosen.size());
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(sourcePermanentId(gameData, entry));
        List<UUID> options = new ArrayList<>();
        for (Permanent permanent : List.copyOf(
                gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()))) {
            if (chosen.contains(permanent.getId())
                    || !predicateEvaluationService.matchesPermanentPredicate(permanent, choice.filter(), filterContext)
                    || gameQueryService.cantBeChosen(gameData, permanent)) {
                continue;
            }
            List<UUID> with = new ArrayList<>(chosen);
            with.add(permanent.getId());
            if (with.size() == exile.choices().size() || !options(gameData, entry, exile, with).isEmpty()) {
                options.add(permanent.getId());
            }
        }
        return options;
    }

    private void exileTogether(GameData gameData, StackEntry entry, ExileChosenPermanentsEffect exile,
                               List<UUID> chosen, UUID sourceId) {
        List<Permanent> permanents = new ArrayList<>();
        List<LastKnownCardStats> known = new ArrayList<>();
        for (UUID id : chosen) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                return;
            }
            permanents.add(permanent);
            // Each as it was before any of them left (CR 608.2h).
            known.add(new LastKnownCardStats(id, gameQueryService.getEffectivePower(gameData, permanent),
                    gameQueryService.getEffectiveToughness(gameData, permanent),
                    gameQueryService.getPermanentManaValue(permanent),
                    gameQueryService.getEffectiveName(gameData, permanent)));
        }
        String sourceName = entry.getCard().getName();
        boolean together = permanents.size() > 1;
        if (together) {
            permanentRemovalService.beginPermanentLeaveBatch(gameData);
        }
        try {
            for (int i = 0; i < permanents.size(); i++) {
                Permanent permanent = permanents.get(i);
                List<Card> leaving = List.copyOf(permanent.cardsLeavingBattlefield());
                entry.rememberLastKnownPermanentCard(permanent.getId(), permanent.getCard());
                if (sourceId == null) {
                    exileSupport.exilePermanentAndLog(gameData, permanent, sourceName);
                } else {
                    exileSupport.exilePermanentAndTrackWithSource(gameData, permanent, sourceId, entry.getCard());
                }
                if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
                    continue;
                }
                List<MovedCard> moved = new ArrayList<>();
                for (Card card : leaving) {
                    ExiledCardEntry exiled = card.isToken() ? null : gameData.findExiledCard(card.getId());
                    if (exiled == null) {
                        continue;
                    }
                    moved.add(new MovedCard(permanent.getId(), card.getId(), Zone.EXILE, exiled.ownerId()));
                    if (exile.link() == SourceLink.UNTIL_SOURCE_LEAVES) {
                        gameData.addExileReturnOnPermanentLeave(sourceId,
                                new PendingExileReturn(card, exiled.ownerId()));
                    }
                }
                entry.addToStep(StepKind.EXILE, StepOutcome
                        .ofObjects(List.of(permanent.getId()), List.of(entry.getControllerId()), moved)
                        .withLastKnownPermanents(List.of(known.get(i))));
            }
        } finally {
            if (together) {
                permanentRemovalService.endPermanentLeaveBatch(gameData);
            }
        }
    }

    private static UUID sourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        if (entry.getSourcePermanentSnapshot() != null) {
            return entry.getSourcePermanentSnapshot().getId();
        }
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return null;
    }
}
