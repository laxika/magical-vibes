package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesTwoDistinctPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves "target player sacrifices a [first] and a [second]". The player chooses a permanent for
 * each filter (the first choice is limited to permanents that still leave a different permanent for
 * the second filter, so as many permanents as possible are sacrificed), and everything chosen is
 * sacrificed at the same time once both choices are made.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesTwoDistinctPermanentsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesTwoDistinctPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerSacrificesTwoDistinctPermanentsEffect) effect;
        UUID playerId = entry.getTargetId();
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
            return;
        }

        List<UUID> firstCandidates = candidates(gameData, entry, playerId, e.first());
        List<UUID> secondCandidates = candidates(gameData, entry, playerId, e.second());

        if (firstCandidates.isEmpty() || secondCandidates.isEmpty()) {
            List<UUID> only = firstCandidates.isEmpty() ? secondCandidates : firstCandidates;
            if (only.isEmpty()) {
                return;
            }
            offer(gameData, only, List.of(), playerId);
            return;
        }

        List<UUID> firstChoices = firstCandidates.stream()
                .filter(id -> secondCandidates.stream().anyMatch(other -> !other.equals(id)))
                .toList();
        if (firstChoices.isEmpty()) {
            // The same single permanent is the only way to satisfy both filters.
            destructionSupport.performSimultaneousSacrifice(gameData, List.of(firstCandidates.getFirst()));
            return;
        }

        if (firstChoices.size() == 1) {
            UUID first = firstChoices.getFirst();
            List<UUID> secondChoices = secondCandidates.stream().filter(id -> !id.equals(first)).toList();
            offer(gameData, secondChoices, List.of(first), playerId);
            return;
        }

        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        choosers.add(new PendingForcedSacrifice(playerId, 1, firstChoices));
        choosers.add(new PendingForcedSacrifice(playerId, 1, secondCandidates));
        destructionSupport.beginNextForcedSacrificeFromQueue(gameData, choosers, List.of(), true);
    }

    /** Sacrifices the already-fixed choices plus a choice among {@code options}, all at once. */
    private void offer(GameData gameData, List<UUID> options, List<UUID> fixedChoices, UUID playerId) {
        if (options.size() == 1) {
            List<UUID> all = new ArrayList<>(fixedChoices);
            all.add(options.getFirst());
            destructionSupport.performSimultaneousSacrifice(gameData, all);
            return;
        }
        destructionSupport.beginNextForcedSacrificeFromQueue(gameData,
                List.of(new PendingForcedSacrifice(playerId, 1, options)), fixedChoices, true);
    }

    private List<UUID> candidates(GameData gameData, StackEntry entry, UUID playerId,
            PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(source != null ? source.getId() : null);
        return battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(p, filter, context))
                .filter(p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p))
                .map(Permanent::getId)
                .toList();
    }
}
