package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a Reflect ability by offering its payment to each opponent in APNAP order. */
@Component
@RequiredArgsConstructor
public class ReflectEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReflectEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reflect = (ReflectEffect) effect;
        UUID controllerId = reflect.abilityControllerId() != null
                ? reflect.abilityControllerId()
                : entry.getControllerId();
        UUID sourcePermanentId = reflect.sourcePermanentId() != null
                ? reflect.sourcePermanentId()
                : entry.getSourcePermanentId();
        List<UUID> opponents = reflect.remainingOpponentIds() == null
                ? AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, controllerId)
                : new ArrayList<>(reflect.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));

        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new ReflectEffect(
                    reflect.manaCost(), List.copyOf(opponents), controllerId, sourcePermanentId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard, ReflectEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Pay " + effect.manaCost() + " to create a token copy of "
                        + sourceCard.getName() + "?",
                null,
                effect.manaCost(),
                effect.sourcePermanentId()));
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability, ReflectEffect effect) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new ReflectEffect(
                    effect.manaCost(), List.copyOf(remaining), effect.abilityControllerId(),
                    effect.sourcePermanentId()));
        }
    }
}
