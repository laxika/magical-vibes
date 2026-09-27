package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Vial Smasher's random-opponent damage choice at resolution. */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentOrPlaneswalkerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomOpponentOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToRandomOpponentOrPlaneswalkerEffect) effect;
        UUID opponentId = entry.getOpponentChosenTargetPlayerId();

        if (opponentId == null) {
            List<UUID> opponents = new ArrayList<>();
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(entry.getControllerId()) && gameData.playerIds.contains(playerId)) {
                    opponents.add(playerId);
                }
            }
            if (opponents.isEmpty()) {
                return;
            }

            opponentId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
            entry.setOpponentChosenTargetPlayerId(opponentId);
            // Generic non-targeting spell-cast triggers use targetId for contextual "that player";
            // this effect has its own random-opponent context instead.
            entry.setTargetId(null);

            List<UUID> planeswalkerIds = planeswalkerIdsControlledBy(gameData, opponentId);
            if (!planeswalkerIds.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.RandomOpponentDamageChoice(
                                entry.getCard(), entry.getControllerId()));
                playerInputService.beginAnyTargetChoice(
                        gameData,
                        entry.getControllerId(),
                        planeswalkerIds,
                        List.of(opponentId),
                        entry.getCard().getName() + " — Choose that player or a planeswalker they control.");
                return;
            }

            dealDamage(gameData, entry, e, opponentId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        UUID chosenId = entry.getTargetId() == null ? opponentId : entry.getTargetId();
        dealDamage(gameData, entry, e, chosenId);
    }

    private List<UUID> planeswalkerIdsControlledBy(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return ids;
        }
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isPlaneswalker(gameData, permanent)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }

    private void dealDamage(GameData gameData, StackEntry entry,
                            DealDamageToRandomOpponentOrPlaneswalkerEffect effect, UUID targetId) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, effect.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
