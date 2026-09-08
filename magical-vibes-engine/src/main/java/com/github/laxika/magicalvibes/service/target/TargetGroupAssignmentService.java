package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
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
