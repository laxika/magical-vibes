package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferDrawAndCreateTokenEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Bunnies' sequential draw-and-Rabbit choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemptingOfferDrawAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferDrawAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferDrawAndCreateTokenEffect offer =
                (TemptingOfferDrawAndCreateTokenEffect) effect;
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        drawAndCreate(gameData, entry, offer, controllerId);

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? new ArrayList<>(AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                        .apnapOpponents(gameData, controllerId))
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new TemptingOfferDrawAndCreateTokenEffect(
                    offer.tokenEffect(), List.copyOf(opponents), controllerId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           TemptingOfferDrawAndCreateTokenEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Draw a card and create the offered token? If you do, "
                        + sourceCard.getName() + "'s controller draws a card and creates another one."));
        log.info("Game {} - offering {} the {} draw-and-token choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               TemptingOfferDrawAndCreateTokenEffect effect, boolean accepted) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (accepted && entry != null) {
            drawAndCreate(gameData, entry, effect, ability.controllerId());
            drawAndCreate(gameData, entry, effect, effect.abilityControllerId());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new TemptingOfferDrawAndCreateTokenEffect(
                    effect.tokenEffect(), List.copyOf(remaining), effect.abilityControllerId()));
        }
    }

    private void drawAndCreate(GameData gameData, StackEntry entry,
                               TemptingOfferDrawAndCreateTokenEffect offer, UUID playerId) {
        if (playerId == null) {
            return;
        }
        playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        createTokenEffectHandler.resolveForController(gameData, entry, offer.tokenEffect(), playerId);
    }
}
