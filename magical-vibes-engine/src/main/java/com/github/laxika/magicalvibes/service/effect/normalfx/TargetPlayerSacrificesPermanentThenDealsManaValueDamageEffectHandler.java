package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted sacrifice followed by damage based on the sacrificed permanent's mana value. */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, entry.getControllerId())) {
            return;
        }

        List<UUID> validIds = matchingPermanentIds(gameData, targetPlayerId, sacrifice.filter());
        if (validIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no matching permanent to sacrifice."));
            return;
        }

        if (validIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, validIds.getFirst());
            sacrificeAndDealDamage(gameData, permanent, targetPlayerId, entry, sacrifice.filter());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TargetPlayerSacrificesPermanentThenDealsManaValueDamage(
                        targetPlayerId, entry, sacrifice.filter()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, validIds,
                entry.getCard().getName() + " — Choose a permanent to sacrifice.");
    }

    public void sacrificeAndDealDamage(GameData gameData, Permanent permanent, UUID sacrificingPlayerId,
                                       StackEntry entry, PermanentPredicate filter) {
        if (permanent == null
                || !sacrificingPlayerId.equals(gameQueryService.findPermanentController(gameData, permanent.getId()))
                || !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)
                || gameQueryService.cantBeSacrificed(gameData, permanent)
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificingPlayerId, entry.getControllerId())) {
            return;
        }

        int manaValue = permanent.getCard().getManaValue();
        destructionSupport.sacrificeAndLog(gameData, permanent, sacrificingPlayerId);

        if (manaValue > 0) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, sacrificingPlayerId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private List<UUID> matchingPermanentIds(GameData gameData, UUID playerId, PermanentPredicate filter) {
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return validIds;
        }
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)
                    && !gameQueryService.cantBeSacrificed(gameData, permanent)) {
                validIds.add(permanent.getId());
            }
        }
        return validIds;
    }
}
