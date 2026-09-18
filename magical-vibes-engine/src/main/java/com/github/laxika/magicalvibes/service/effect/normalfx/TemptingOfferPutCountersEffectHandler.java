package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferPutCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Glory's sequential opponent counter choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemptingOfferPutCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferPutCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferPutCountersEffect offer = (TemptingOfferPutCountersEffect) effect;
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        putCounterOnEachCreature(gameData, entry, controllerId);

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? new ArrayList<>(AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                        .apnapOpponents(gameData, controllerId))
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new TemptingOfferPutCountersEffect(
                    List.copyOf(opponents), controllerId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard, TemptingOfferPutCountersEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Put a +1/+1 counter on each creature you control? If you do, "
                        + sourceCard.getName() + " puts another +1/+1 counter on each creature its "
                        + "controller controls."));
        log.info("Game {} - offering {} the {} counter choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               TemptingOfferPutCountersEffect effect, boolean accepted) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (accepted) {
            putCounterOnEachCreature(gameData, entry, ability.controllerId());
            putCounterOnEachCreature(gameData, entry, effect.abilityControllerId());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new TemptingOfferPutCountersEffect(
                    List.copyOf(remaining), effect.abilityControllerId()));
        }
    }

    private void putCounterOnEachCreature(GameData gameData, StackEntry entry, UUID playerId) {
        if (playerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }
        List<Permanent> creatures = battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        for (Permanent creature : creatures) {
            permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, creature, 1);
        }
    }
}
