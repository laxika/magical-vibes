package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfCombatDamageDealersToHandThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Kaito's choice among the creatures from the triggering combat-damage event. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnOneOfCombatDamageDealersToHandThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOneOfCombatDamageDealersToHandThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnOneOfCombatDamageDealersToHandThenEffect) effect;
        List<UUID> validIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (e.combatDamageDealerIds().contains(permanent.getId())) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            gameLogService.append(gameData,
                    GameLog.text(playerName + " controls no " + e.permanentDescription() + " to return."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BouncePermanentThen(
                entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), e.thenEffect()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validIds,
                entry.getCard().getName() + " — Choose " + e.permanentDescription()
                        + " to return to its owner's hand.");
        log.info("Game {} - {} choosing {} to return for {}", gameData.id,
                gameData.playerIdToName.get(entry.getControllerId()), e.permanentDescription(), entry.getCard().getName());
    }
}
