package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect} (Goblin
 * Vandal). The stack entry's {@code targetId} is the defending player and {@code sourcePermanentId}
 * the attacking creature. Presents a max-1 choice among the defending player's permanents matching
 * the effect's filter; the destruction and the "assigns no combat damage" rider are applied in
 * {@code MultiPermanentChoiceHandlerService} when a permanent is chosen.
 */
@Component
@RequiredArgsConstructor
public class DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect) effect;
        UUID defenderId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (defenderId == null || sourcePermanentId == null) {
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
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text("'s ability resolves, but " + gameData.playerIdToName.get(defenderId)
                            + " controls no " + e.choiceNoun() + "s.").build());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), validIds, 1,
                new MultiPermanentChoiceContext.DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage(
                        sourcePermanentId, e.choiceNoun()),
                entry.getCard().getName() + "'s ability — Choose a " + e.choiceNoun() + " "
                        + gameData.playerIdToName.get(defenderId) + " controls to destroy.");
    }
}
