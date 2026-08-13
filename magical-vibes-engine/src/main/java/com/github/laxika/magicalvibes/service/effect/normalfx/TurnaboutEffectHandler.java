package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnaboutEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the resolution-time choices and mass tap/untap operation of Turnabout. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurnaboutEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TapUntapSupport tapUntapSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnaboutEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellPermanentType == null || gameData.turnaboutTap == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginTurnaboutChoice(gameData, entry.getControllerId());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardType chosenType = gameData.chosenSpellPermanentType;
        boolean tap = gameData.turnaboutTap;
        gameData.chosenSpellPermanentType = null;
        gameData.turnaboutTap = null;

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getTargetId());
        if (battlefield == null) {
            return;
        }

        PermanentPredicate typePredicate = switch (chosenType) {
            case ARTIFACT -> new PermanentIsArtifactPredicate();
            case CREATURE -> new PermanentIsCreaturePredicate();
            case LAND -> new PermanentIsLandPredicate();
            default -> throw new IllegalStateException("Unsupported Turnabout type: " + chosenType);
        };
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int changed = 0;
        for (Permanent permanent : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, typePredicate, filterContext)) {
                continue;
            }
            if (tap && !permanent.isTapped() && tapUntapSupport.tapPermanent(gameData, permanent)) {
                changed++;
            } else if (!tap && permanent.isTapped() && tapUntapSupport.untapPermanent(gameData, permanent)) {
                changed++;
            }
        }

        String action = tap ? " taps " : " untaps ";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(action + changed + " " + chosenType.getDisplayName().toLowerCase() + " permanent(s).")
                .build());
        log.info("Game {} - {} {} {} {} permanent(s) controlled by target player",
                gameData.id, entry.getCard().getName(), tap ? "taps" : "untaps", changed,
                chosenType.getDisplayName().toLowerCase());
    }
}
