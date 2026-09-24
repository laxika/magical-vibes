package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokensEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Vengeance's sequential opponent token choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemptingOfferCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferCreateTokensEffect offer = (TemptingOfferCreateTokensEffect) effect;
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        createTokens(gameData, entry, offer, controllerId);

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? new ArrayList<>(AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                        .apnapOpponents(gameData, controllerId))
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new TemptingOfferCreateTokensEffect(
                    offer.tokenEffect(), List.copyOf(opponents), controllerId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard, TemptingOfferCreateTokensEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Create X 1/1 red Elemental creature tokens? If you do, "
                        + sourceCard.getName() + "'s controller creates X more."));
        log.info("Game {} - offering {} the {} token choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               TemptingOfferCreateTokensEffect effect, boolean accepted) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (accepted && entry != null) {
            createTokens(gameData, entry, effect, ability.controllerId());
            createTokens(gameData, entry, effect, effect.abilityControllerId());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new TemptingOfferCreateTokensEffect(
                    effect.tokenEffect(), List.copyOf(remaining), effect.abilityControllerId()));
        }
    }

    private void createTokens(GameData gameData, StackEntry entry,
                              TemptingOfferCreateTokensEffect offer, UUID controllerId) {
        if (controllerId == null) {
            return;
        }
        createTokenEffectHandler.resolveForController(gameData, entry, offer.tokenEffect(), controllerId);
    }
}
