package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Assigns overlapping target cards to distinct declared color groups. */
@Service
@RequiredArgsConstructor
public class TargetGroupAssignmentService {

    private static final List<CardColor> COLORS = List.of(
            CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    /** Assigns each occurrence to one distinct optional permanent target group. */
    public Optional<Assignment> assignOptionalPermanentGroups(GameData gameData, Card card,
            UUID controllerId, List<UUID> targetIds) {
        List<SpellTarget> groups = card.getSpellTargets();
        int[] groupToTarget = new int[groups.size()];
        Arrays.fill(groupToTarget, -1);
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(controllerId)
                .withSourceCardId(card.getId());
        if (!assignPermanentTarget(gameData, targetIds, groups, context, groupToTarget, 0)) {
            return Optional.empty();
        }
        List<UUID> ordered = new ArrayList<>();
        List<Integer> sizes = new ArrayList<>();
        for (int position : groupToTarget) {
            sizes.add(position < 0 ? 0 : 1);
            if (position >= 0) ordered.add(targetIds.get(position));
        }
        return Optional.of(new Assignment(ordered, sizes));
    }

    private boolean assignPermanentTarget(GameData gameData, List<UUID> targets, List<SpellTarget> groups,
            FilterContext context, int[] groupToTarget, int targetIndex) {
        if (targetIndex == targets.size()) return true;
        Permanent permanent = gameQueryService.findPermanentById(gameData, targets.get(targetIndex));
        if (permanent == null) return false;
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            if (groupToTarget[groupIndex] >= 0) continue;
            if (!(groups.get(groupIndex).getFilter() instanceof PermanentPredicateTargetFilter filter)
                    || !predicateEvaluationService.matchesPermanentPredicate(permanent, filter.predicate(), context)) continue;
            groupToTarget[groupIndex] = targetIndex;
            if (assignPermanentTarget(gameData, targets, groups, context, groupToTarget, targetIndex + 1)) return true;
            groupToTarget[groupIndex] = -1;
        }
        return false;
    }

    public Optional<Assignment> assignDistinctColors(GameData gameData, List<UUID> targetIds) {
        if (targetIds == null || targetIds.size() > COLORS.size()
                || targetIds.stream().distinct().count() != targetIds.size()) {
            return Optional.empty();
        }

        List<Set<CardColor>> targetColors = new ArrayList<>(targetIds.size());
        for (UUID targetId : targetIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, targetId);
            if (card == null) {
                return Optional.empty();
            }
            targetColors.add(gameQueryService.getEffectiveCardColors(gameData, card));
        }

        int[] colorToTarget = new int[COLORS.size()];
        Arrays.fill(colorToTarget, -1);
        for (int targetIndex = 0; targetIndex < targetColors.size(); targetIndex++) {
            boolean[] seenColors = new boolean[COLORS.size()];
            if (!assignTarget(targetIndex, targetColors, colorToTarget, seenColors)) {
                return Optional.empty();
            }
        }

        List<UUID> orderedTargetIds = new ArrayList<>(targetIds.size());
        List<Integer> groupSizes = new ArrayList<>(COLORS.size());
        for (int colorIndex = 0; colorIndex < COLORS.size(); colorIndex++) {
            int targetIndex = colorToTarget[colorIndex];
            if (targetIndex >= 0) {
                orderedTargetIds.add(targetIds.get(targetIndex));
                groupSizes.add(1);
            } else {
                groupSizes.add(0);
            }
        }
        return Optional.of(new Assignment(orderedTargetIds, groupSizes));
    }

    private boolean assignTarget(int targetIndex, List<Set<CardColor>> targetColors,
                                 int[] colorToTarget, boolean[] seenColors) {
        for (int colorIndex = 0; colorIndex < COLORS.size(); colorIndex++) {
            if (seenColors[colorIndex] || !targetColors.get(targetIndex).contains(COLORS.get(colorIndex))) {
                continue;
            }
            seenColors[colorIndex] = true;
            int previousTarget = colorToTarget[colorIndex];
            if (previousTarget < 0
                    || assignTarget(previousTarget, targetColors, colorToTarget, seenColors)) {
                colorToTarget[colorIndex] = targetIndex;
                return true;
            }
        }
        return false;
    }

    public record Assignment(List<UUID> orderedTargetIds, List<Integer> groupSizes) {
        public Assignment {
            orderedTargetIds = List.copyOf(orderedTargetIds);
            groupSizes = List.copyOf(groupSizes);
        }
    }
}
