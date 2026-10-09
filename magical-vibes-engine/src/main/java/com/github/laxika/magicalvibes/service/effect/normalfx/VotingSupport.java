package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Shared support for static abilities that grant players extra votes. */
@Component
@RequiredArgsConstructor
public class VotingSupport {

    private final GameQueryService gameQueryService;

    public List<UUID> addAdditionalControllerVotes(GameData gameData, List<UUID> voters,
                                                     UUID effectControllerId) {
        List<UUID> result = new ArrayList<>();
        for (UUID voter : voters) {
            result.add(voter);
            for (int i = 0; i < gameQueryService.countAdditionalVotes(gameData, voter); i++) {
                result.add(voter);
            }
        }
        return result;
    }
}
