package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The two halves of populate (CR 701.36): finding the creature tokens a player controls, and
 * creating the copy of the one they chose.
 *
 * <p>{@link PopulateEffectHandler} needs the first half to decide between "no token, do nothing",
 * "one token, forced choice" and "prompt"; {@code PermanentChoiceBattlefieldHandlerService} needs
 * the second half once the prompt is answered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopulateSupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    /** The creature tokens {@code controllerId} controls — the legal populate choices. */
    public List<Permanent> creatureTokensControlledBy(GameData gameData, UUID controllerId) {
        List<Permanent> tokens = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return tokens;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().isToken() && gameQueryService.isCreature(gameData, permanent)) {
                tokens.add(permanent);
            }
        }
        return tokens;
    }

    /** Creates a token copy of {@code sourceToken} for {@code controllerId}, once per token multiplier. */
    public void createCopy(GameData gameData, UUID controllerId, Permanent sourceToken) {
        Card sourceCard = sourceToken.getCard();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);

        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                    sourceCard, new CreateTokenCopyOfTargetPermanentEffect());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, new Permanent(tokenCard));

            gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
            log.info("Game {} - Populate creates a token copy of {}", gameData.id, sourceCard.getName());

            // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
            // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
        }
    }
}
