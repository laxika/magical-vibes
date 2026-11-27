package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ChosenOpponentFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChosenOpponentContinuation;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Shared Fate's replaced draw once the drawing player has chosen whose library it exiles from: the exile, then the
 * waiting draw instruction goes on ({@link InputCompletionService} resumes the pending draws).
 */
@Component
@RequiredArgsConstructor
public class SharedFateLibraryContinuation
        implements ChosenOpponentContinuation<ChosenOpponentFollowUp.SharedFateExile> {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<ChosenOpponentFollowUp.SharedFateExile> followUpType() {
        return ChosenOpponentFollowUp.SharedFateExile.class;
    }

    @Override
    public void continueWithOpponent(GameData gameData, UUID opponentId, ChosenOpponentFollowUp.SharedFateExile exile) {
        Permanent source = gameQueryService.findPermanentById(gameData, exile.sourcePermanentId());
        if (source != null) {
            drawService.exileWithSharedFate(gameData, exile.drawingPlayerId(), opponentId, source);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
