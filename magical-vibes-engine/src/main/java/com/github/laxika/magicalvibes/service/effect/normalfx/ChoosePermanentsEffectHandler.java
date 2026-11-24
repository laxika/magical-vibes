package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext.RecordChosenPermanents.Offer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link ChoosePermanentsEffect}: asks for a single permanent or for several, pauses at its end on the choice
 * and records {@link StepKind#CHOOSE} as it is answered. The log names no face-down permanent's card (CR 708.2).
 * The choosing player is the effect's {@link ChoosePermanentsEffect#choosingPlayer()}, the controller unless the text
 * names another ("target opponent chooses a creature they control"); with none left to choose, nothing is chosen.
 */
@Component
@RequiredArgsConstructor
public class ChoosePermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;
    private final ValidTargetService validTargetService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChoosePermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChoosePermanentsEffect) effect;
        List<Permanent> candidates = attachFits(gameData, entry, choose,
                subjectResolutionService.permanents(gameData, entry, effect, choose.candidates()).stream()
                        .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent)).toList());
        entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
        UUID chooserId = chooserId(gameData, entry, choose);
        if (chooserId == null) {
            return;
        }
        if (choose.sacrificing()) {
            // One to sacrifice: a permanent the chooser couldn't sacrifice is no option (CR 608.2d, 701.21a).
            candidates = candidates.stream()
                    .filter(permanent -> chooserId.equals(gameQueryService.findPermanentController(gameData,
                            permanent.getId())))
                    .filter(permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent))
                    .toList();
        }
        AmountContext context = AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot());
        int maximum = choose.maximum() == null ? candidates.size() : Math.min(candidates.size(),
                amountEvaluationService.evaluate(gameData, choose.maximum(), context));
        if (maximum <= 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": there is nothing to choose."));
            return;
        }
        if (choose.random()) {
            List<Permanent> drawn = new ArrayList<>(candidates);
            Collections.shuffle(drawn, ThreadLocalRandom.current());
            record(gameData, entry, choose, List.copyOf(drawn.subList(0, maximum)));
            return;
        }
        if (choose.oneForEach() != null) {
            String sourceName = FaceDownVisibility.sourceName(gameData, entry);
            List<Offer> choices = new ArrayList<>();
            for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, choose.oneForEach())) {
                List<UUID> theirs = candidates.stream()
                        .map(Permanent::getId)
                        .filter(id -> playerId.equals(gameQueryService.findPermanentController(gameData, id)))
                        .toList();
                if (!theirs.isEmpty()) {
                    choices.add(new Offer(entry.getControllerId(), theirs, sourceName + " — choose " + choose.label()
                            + " " + gameData.playerIdToName.get(playerId) + " controls."));
                }
            }
            askForEach(gameData, entry, choose, List.of(), choices);
            return;
        }
        if (choose.forEachVote() != null) {
            askForEach(gameData, entry, choose, List.of(), voteOffers(gameData, entry, choose, candidates));
            return;
        }
        int minimum = Math.min(maximum,
                Math.max(0, amountEvaluationService.evaluate(gameData, choose.minimum(), context)));
        if (minimum == candidates.size()) {
            record(gameData, entry, choose, candidates);
            return;
        }
        List<UUID> ids = candidates.stream().map(Permanent::getId).toList();
        String prompt = FaceDownVisibility.sourceName(gameData, entry) + " — choose " + choose.label() + ".";
        if (minimum == 1 && maximum == 1) {
            var single = new PermanentChoiceContext.RecordChosenPermanents(choose);
            gameData.interaction.setPermanentChoiceContext(single);
            playerInputService.beginPermanentChoice(gameData, chooserId, ids, single, prompt);
        } else {
            playerInputService.beginMultiPermanentChoice(gameData, chooserId, ids, maximum,
                    new MultiPermanentChoiceContext.ChoosePermanents(choose, minimum), prompt);
        }
    }

    /**
     * {@link ChoosePermanentsEffect.ForEachVote}: one offer per vote for its option in the resolution's last vote, in
     * the order the votes were cast, of the candidates that vote's voter controls or owns; a voter with none gets none.
     */
    private List<Offer> voteOffers(GameData gameData, StackEntry entry, ChoosePermanentsEffect choose,
                                   List<Permanent> candidates) {
        ChoosePermanentsEffect.ForEachVote perVote = choose.forEachVote();
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        List<Offer> offers = new ArrayList<>();
        for (StepOutcome.Vote vote : entry.getResolutionResults().outcome(StepKind.VOTE).votes()) {
            UUID voterId = vote.voterId();
            if (vote.candidateId() != null || !perVote.option().equals(vote.choice())
                    || !gameData.playerIds.contains(voterId)) {
                continue;
            }
            List<UUID> theirs = candidates.stream()
                    .map(Permanent::getId)
                    .filter(id -> voterId.equals(perVote.relation() == ChoosePermanentsEffect.VoterRelation.OWNS
                            ? gameData.defaultControllerOf(id) : gameQueryService.findPermanentController(gameData, id)))
                    .toList();
            if (theirs.isEmpty()) {
                continue;
            }
            String whose = perVote.relation() == ChoosePermanentsEffect.VoterRelation.OWNS
                    ? " owned by " + (perVote.byVoter() ? "you" : gameData.playerIdToName.get(voterId))
                    : " " + (perVote.byVoter() ? "you control" : gameData.playerIdToName.get(voterId) + " controls");
            offers.add(new Offer(perVote.byVoter() ? voterId : entry.getControllerId(), theirs,
                    sourceName + " — choose " + choose.label() + whose + " (" + perVote.option().toLowerCase()
                            + " vote)."));
        }
        return offers;
    }

    /**
     * {@code candidates} narrowed by the choice's attach restrictions ({@link ChoosePermanentsEffect#attachableTo()},
     * {@link ChoosePermanentsEffect#hostFor()}), judged by the attach verb's legality ({@link EquipSupport#canAttach});
     * an attachment already attached to the host is no option either, as attaching it there would do nothing
     * (CR 701.3b). With {@link ChoosePermanentsEffect#triggeringSpellCouldTarget()}, only those the triggering spell
     * could target.
     */
    private List<Permanent> attachFits(GameData gameData, StackEntry entry, ChoosePermanentsEffect choose,
                                       List<Permanent> candidates) {
        List<Permanent> fits = candidates;
        if (choose.attachableTo() != null) {
            Permanent host = subjectResolutionService.permanent(gameData, entry, choose, choose.attachableTo());
            fits = host == null ? List.of() : fits.stream()
                    .filter(candidate -> !host.getId().equals(candidate.getAttachedTo())
                            && equipSupport.canAttach(gameData, candidate, host)).toList();
        }
        if (choose.hostFor() != null) {
            List<Permanent> attachments = subjectResolutionService.permanents(gameData, entry, choose,
                    choose.hostFor());
            fits = attachments.isEmpty() ? List.of() : fits.stream()
                    .filter(candidate -> attachments.stream().allMatch(attachment ->
                            !attachment.getId().equals(candidate.getId())
                                    && !candidate.getId().equals(attachment.getAttachedTo())
                                    && equipSupport.canAttach(gameData, attachment, candidate)))
                    .toList();
        }
        if (choose.triggeringSpellCouldTarget()) {
            StackEntry spell = CopySupport.triggeringSpell(gameData, entry);
            Set<UUID> couldTarget = spell == null ? Set.of()
                    : validTargetService.permanentIdsSpellCouldTarget(gameData, spell);
            fits = fits.stream().filter(candidate -> couldTarget.contains(candidate.getId())).toList();
        }
        return fits;
    }

    /**
     * One choice after another, a lone candidate taken unasked; returns whether a player is asked. What is chosen is
     * recorded once the last choice is made.
     */
    private boolean askForEach(GameData gameData, StackEntry entry, ChoosePermanentsEffect choose,
                               List<UUID> chosenBefore, List<Offer> choices) {
        List<UUID> chosen = new ArrayList<>(chosenBefore);
        for (int i = 0; i < choices.size(); i++) {
            Offer offer = choices.get(i);
            List<UUID> offered = offer.permanentIds().stream()
                    .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                    .toList();
            if (offered.size() <= 1) {
                chosen.addAll(offered);
                continue;
            }
            var next = new PermanentChoiceContext.RecordChosenPermanents(
                    choose, List.copyOf(chosen), List.copyOf(choices.subList(i + 1, choices.size())));
            gameData.interaction.setPermanentChoiceContext(next);
            playerInputService.beginPermanentChoice(gameData, offer.chooserId(), offered, next, offer.prompt());
            return true;
        }
        record(gameData, entry, choose, present(gameData, chosen));
        return false;
    }

    /** The answer to a choice of one; returns whether the choice for the next player follows. */
    public boolean completeSingleChoice(GameData gameData, UUID chosenId,
                                        PermanentChoiceContext.RecordChosenPermanents context) {
        if (context.effect().oneForEach() == null && context.effect().forEachVote() == null) {
            completeChoice(gameData, List.of(chosenId), context.effect());
            return false;
        }
        List<UUID> chosen = new ArrayList<>(context.chosenBefore());
        chosen.add(chosenId);
        return askForEach(gameData, gameData.pendingEffectResolutionEntry, context.effect(), chosen,
                context.laterChoices());
    }

    /** Rejects an answer before anything is changed: too few, or two that share a quality that must differ. */
    public void validateChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.ChoosePermanents context) {
        if (chosenIds.size() < context.minimum()) {
            throw new IllegalStateException("At least " + context.minimum() + " permanents must be selected");
        }
        ChoosePermanentsEffect.Different different = context.effect().different();
        if (different == null) {
            return;
        }
        Set<Integer> values = new HashSet<>();
        for (UUID id : chosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                continue;
            }
            int value = switch (different) {
                case POWER -> gameQueryService.getEffectivePower(gameData, permanent);
                case MANA_VALUE -> gameQueryService.getPermanentManaValue(permanent);
            };
            if (!values.add(value)) {
                throw new IllegalStateException("The selected permanents must have different " + different.plural());
            }
        }
    }

    /** The answer to the choice. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds, ChoosePermanentsEffect choose) {
        record(gameData, gameData.pendingEffectResolutionEntry, choose, present(gameData, chosenIds));
    }

    /** The permanents of {@code ids} still on the battlefield, each once (one chosen for two votes is chosen once). */
    private List<Permanent> present(GameData gameData, List<UUID> ids) {
        List<Permanent> present = new ArrayList<>();
        for (UUID id : new java.util.LinkedHashSet<>(ids)) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                present.add(permanent);
            }
        }
        return present;
    }

    /** The player who chooses: the controller, or the one the effect names as it resolves; {@code null} for none. */
    private UUID chooserId(GameData gameData, StackEntry entry, ChoosePermanentsEffect choose) {
        if (choose.chooser() == null) {
            return entry.getControllerId();
        }
        List<UUID> players = subjectResolutionService.players(gameData, entry, choose, choose.chooser());
        return players.isEmpty() ? null : players.getFirst();
    }

    private void record(GameData gameData, StackEntry entry, ChoosePermanentsEffect choose, List<Permanent> chosen) {
        UUID chooserId = chooserId(gameData, entry, choose);
        String chooser = gameData.playerIdToName.get(chooserId != null ? chooserId : entry.getControllerId());
        if (chosen.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(chooser + " chooses nothing."));
            return;
        }
        String chooses = choose.random() ? "Chosen at random: "
                : choose.forEachVote() != null && choose.forEachVote().byVoter() ? "Chosen by the voters: "
                : chooser + " chooses ";
        List<UUID> controllers = new ArrayList<>();
        var log = GameLog.builder().text(chooses);
        for (int i = 0; i < chosen.size(); i++) {
            Permanent permanent = chosen.get(i);
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId != null) {
                controllers.add(controllerId);
            }
            if (i > 0) {
                log.text(", ");
            }
            if (permanent.isFaceDown()) {
                log.text("a face-down permanent");
            } else {
                log.card(permanent.getCard());
            }
        }
        gameLogService.append(gameData, log.text(".").build());
        entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(
                chosen.stream().map(Permanent::getId).toList(), controllers));
        if (choose.oneForEach() == null && choose.forEachVote() == null && new Fixed(1).equals(choose.maximum())) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                source.setChosenPermanentId(chosen.getFirst().getId());
            }
        }
    }
}
