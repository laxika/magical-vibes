package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect}
 * (Orcish Squatters, Kukemssa Pirates). The stack entry's {@code targetId} is the defending player
 * and {@code sourcePermanentId} the attacking creature. Presents a max-1 choice among the defending
 * player's permanents matching the effect's filter; taking control (for the effect's duration) and
 * the "assigns no combat damage" rider are applied in {@code MultiPermanentChoiceHandlerService}
 * when a permanent is chosen.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect) effect;
        UUID defenderId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID controllerId = entry.getControllerId();

        if (defenderId == null || sourcePermanentId == null) {
            return;
        }

        // Per ruling: for a source-linked duration, if the source is gone when this resolves it
        // does nothing. Permanent control (Kukemssa Pirates) still happens without the source.
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null && e.duration().isSourceLinked()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability has no effect (source left the battlefield)."));
            return;
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.filter())) {
                    validIds.add(perm.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text("'s ability resolves, but " + gameData.playerIdToName.get(defenderId) + " controls no " + e.choiceNoun() + "s.").build());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, 1,
                new MultiPermanentChoiceContext.GainControlOfPermanentAndAssignNoCombatDamage(
                        sourcePermanentId, e.duration(), e.choiceNoun()),
                entry.getCard().getName() + "'s ability — Choose a " + e.choiceNoun() + " "
                        + gameData.playerIdToName.get(defenderId) + " controls to gain control of.");
    }
}
