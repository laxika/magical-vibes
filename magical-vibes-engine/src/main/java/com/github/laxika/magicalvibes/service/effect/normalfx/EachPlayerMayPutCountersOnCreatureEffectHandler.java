package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetPermanentThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Queues the optional counter choice for each player in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCountersOnCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayPutCountersOnCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterEffect = (EachPlayerMayPutCountersOnCreatureEffect) effect;
        for (UUID playerId : apnapPlayers(gameData)) {
            if (!hasCreature(gameData, playerId)) {
                continue;
            }

            PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledByPlayerPredicate(playerId)));
            CardEffect acceptedFollowUp = counterEffect.acceptedFollowUp() == null
                    ? new CreaturesCantAttackControllerUntilNextTurnEffect(playerId, true)
                    : counterEffect.acceptedFollowUp();
            CardEffect counterThenRestriction = new PutCountersOnTargetPermanentThenReflexiveEffect(
                    counterEffect.counterType(), counterEffect.count(), null, acceptedFollowUp,
                    false, targetPredicate, counterEffect.acceptedFollowUp() != null);
            String counterText = counterEffect.count() + " "
                    + counterEffect.counterType().name().toLowerCase().replace('_', ' ')
                    + " counter" + (counterEffect.count() == 1 ? "" : "s");
            gameData.queueMayAbilityForPlayer(
                    entry.getCard(), entry.getControllerId(),
                    new MayEffect(counterThenRestriction,
                            "Put " + counterText + " on a creature you control?"),
                    null, entry.getSourcePermanentId(), playerId,
                    entry.getSourcePermanentSnapshot());
        }
    }

    private boolean hasCreature(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .anyMatch(permanent -> gameQueryService.isCreature(gameData, permanent));
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered;
    }
}
