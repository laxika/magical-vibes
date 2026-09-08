package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsThenDrawPerTappedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TapAnyNumberOfPermanentsThenDrawPerTappedEffect}: the controller chooses any
 * number of matching untapped permanents, then the choice completion taps them and draws one card
 * per permanent actually tapped.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TapAnyNumberOfPermanentsThenDrawPerTappedEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAnyNumberOfPermanentsThenDrawPerTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapAnyNumberOfPermanentsThenDrawPerTappedEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!permanent.isTapped()
                        && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, e.filter())) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" resolves, but there are no eligible untapped permanents to tap.").build());
            log.info("Game {} - {} has no eligible untapped permanents to tap",
                    gameData.id, entry.getCard().getName());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                new MultiPermanentChoiceContext.TapPermanentsDrawPerTapped(),
                "Tap any number of untapped Gates you control. Draw a card for each Gate tapped this way.");
    }
}
