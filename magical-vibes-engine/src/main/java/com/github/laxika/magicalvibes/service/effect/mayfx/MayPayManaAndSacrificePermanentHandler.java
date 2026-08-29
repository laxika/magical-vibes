package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MayPayManaAndSacrificePermanentHandler implements MayEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayManaAndSacrificePermanentEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayPayManaAndSacrificePermanentEffect effect = ability.effects().stream()
                .filter(e -> e instanceof MayPayManaAndSacrificePermanentEffect)
                .map(MayPayManaAndSacrificePermanentEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (!accepted) {
            gameLogService.append(gameData, GameLog.playerDeclinesAbility(
                    player.getUsername(), ability.sourceCard()));
            gameData.resolvedMayAccepted = false;
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID controllerId = ability.controllerId();
        List<UUID> matchingIds = matchingPermanentIds(gameData, ability, effect);
        ManaCost cost = new ManaCost(effect.manaCost());
        var pool = gameData.playerManaPools.get(controllerId);
        if (matchingIds.isEmpty() || pool == null || !cost.canPay(pool)) {
            gameData.resolvedMayAccepted = false;
            if (matchingIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(controllerId) + " has no "
                                + effect.permanentDescription() + " to sacrifice."));
            } else {
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " cannot pay " + effect.manaCost() + " for ",
                        ability.sourceCard(), "'s ability."));
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        cost.pay(pool);
        gameData.resolvedMayAccepted = true;
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " pays " + effect.manaCost() + " and chooses a "
                        + effect.permanentDescription() + " for ", ability.sourceCard(), "."));
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificePermanentThen(
                        controllerId, ability.sourceCard(), effect.thenEffect()));
        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                matchingIds,
                ability.sourceCard().getName() + " - Choose "
                        + effect.permanentDescription() + " to sacrifice.");
    }

    private List<UUID> matchingPermanentIds(
            GameData gameData, PendingMayAbility ability,
            MayPayManaAndSacrificePermanentEffect effect) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(ability.sourceCard().getId())
                .withSourceControllerId(ability.controllerId())
                .withSourcePermanentId(ability.sourcePermanentId());
        List<UUID> matchingIds = new ArrayList<>();
        var battlefield = gameData.playerBattlefields.get(ability.controllerId());
        if (battlefield != null) {
            for (var permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, effect.filter(), filterContext)) {
                    matchingIds.add(permanent.getId());
                }
            }
        }
        return matchingIds;
    }
}
