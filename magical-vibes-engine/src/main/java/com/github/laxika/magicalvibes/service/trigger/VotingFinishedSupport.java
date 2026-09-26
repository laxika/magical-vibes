package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.VotingResult;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Collects vote choices while a vote resolves and publishes its completed snapshot. */
@Component
@RequiredArgsConstructor
public class VotingFinishedSupport {

    private final TriggerCollectionService triggerCollectionService;

    public void recordVote(GameData gameData, UUID controllerId, UUID voterId, String choice) {
        gameData.votingChoicesByController
                .computeIfAbsent(controllerId, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(voterId, ignored -> new LinkedHashSet<>())
                .add(choice);
    }

    public void finishVoting(GameData gameData, UUID controllerId) {
        Map<UUID, Set<String>> choices = gameData.votingChoicesByController.remove(controllerId);
        triggerCollectionService.checkVotingFinishedTriggers(
                gameData, new VotingResult(choices == null ? Map.of() : choices));
    }
}
