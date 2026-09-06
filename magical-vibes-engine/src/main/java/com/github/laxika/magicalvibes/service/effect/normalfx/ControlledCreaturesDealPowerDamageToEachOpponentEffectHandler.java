package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealPowerDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlledCreaturesDealPowerDamageToEachOpponentEffectHandler
        implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledCreaturesDealPowerDamageToEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ControlledCreaturesDealPowerDamageToEachOpponentEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);
        List<Permanent> sources = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), context)) {
                sources.add(permanent);
            }
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        for (Permanent source : sources) {
            if (gameQueryService.findPermanentById(gameData, source.getId()) == null) {
                continue;
            }
            UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (sourceControllerId == null) {
                continue;
            }
            for (UUID opponentId : opponents) {
                StackEntry damageEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        sourceControllerId,
                        source.getCard().getName() + "'s ability",
                        List.of(),
                        opponentId,
                        source.getId());
                dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry,
                        new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.TARGET_PLAYER));
            }
        }
    }
}
