package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "Sacrifice this permanent unless you sacrifice a [filtered permanent]." If the controller
 * has nothing matching, the source is sacrificed immediately; otherwise they are asked.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeUnlessSacrificeOwnPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeUnlessSacrificeOwnPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeUnlessSacrificeOwnPermanentEffect) effect;

        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        Permanent sourcePermanent = null;
        boolean hasValidPermanent = false;
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                }
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) {
                    hasValidPermanent = true;
                }
            }
        }

        if (!hasValidPermanent) {
            if (sourcePermanent != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " controls no " + e.description() + " to sacrifice. ")
                        .card(sourceCard)
                        .text(" is sacrificed.")
                        .build());
                log.info("Game {} - {} sacrificed (no {} to sacrifice)", gameData.id, sourceCard.getName(), e.description());
            }
            return;
        }

        String prompt = sourcePermanent != null
                ? "Sacrifice " + e.description() + "? If you don't, " + sourceCard.getName() + " will be sacrificed."
                : sourceCard.getName() + " is no longer on the battlefield. Sacrifice " + e.description() + " anyway?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(e), prompt
        ));
    }
}
