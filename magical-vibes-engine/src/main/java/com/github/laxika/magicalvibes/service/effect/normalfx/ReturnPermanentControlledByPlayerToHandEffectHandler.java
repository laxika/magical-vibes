package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * A player returns a permanent they control matching the effect's filter to its owner's hand. The
 * acting player is the one carried on the entry's {@code targetId} (Mana Breach — "that player" i.e.
 * the caster) or, when no target is set, the resolving controller (Kefnet the Mindful — "you may
 * return a land you control", wrapped in a {@code MayEffect}; Havengul Skaab's attack trigger —
 * "return another creature you control"). Reuses the shared {@code BounceCreature} choice context to
 * prompt that player and return the chosen permanent.
 */
@Component
@RequiredArgsConstructor
public class ReturnPermanentControlledByPlayerToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentControlledByPlayerToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnPermanentControlledByPlayerToHandEffect e = (ReturnPermanentControlledByPlayerToHandEffect) effect;
        UUID playerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        UUID chooserId = e.controllerChooses() ? entry.getControllerId() : playerId;
        if (playerId == null || chooserId == null || !gameData.playerIds.contains(playerId)
                || !gameData.playerIds.contains(chooserId)) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<UUID> choices = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) {
                    choices.add(p.getId());
                }
            }
        }

        if (choices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " controls no " + e.noun() + " to return."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(chooserId));
        playerInputService.beginPermanentChoice(gameData, chooserId, choices,
                "Choose a " + e.noun() + " to return to its owner's hand.");
    }
}
