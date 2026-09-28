package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Shared support for static abilities that grant the effect controller extra votes. */
@Component
@RequiredArgsConstructor
public class VotingSupport {

    private final GameQueryService gameQueryService;

    public List<UUID> addAdditionalControllerVotes(GameData gameData, List<UUID> voters,
                                                     UUID effectControllerId) {
        int additionalVotes = gameQueryService.countAdditionalVotes(gameData, effectControllerId);
        if (additionalVotes == 0) {
            return voters;
        }

        List<UUID> result = new ArrayList<>(voters);
        int controllerIndex = result.indexOf(effectControllerId);
        if (controllerIndex < 0) {
            return result;
        }
        for (int i = 0; i < additionalVotes; i++) {
            result.add(++controllerIndex, effectControllerId);
        }
        return result;
    }
}
