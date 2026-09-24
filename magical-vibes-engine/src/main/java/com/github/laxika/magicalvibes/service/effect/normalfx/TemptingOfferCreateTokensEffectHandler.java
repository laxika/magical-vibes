package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokensEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a Tempting offer token creation sequence in APNAP order. */
@Component
public class TemptingOfferCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;

    public TemptingOfferCreateTokensEffectHandler(CreateTokenEffectHandler createTokenEffectHandler) {
        this.createTokenEffectHandler = createTokenEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferCreateTokensEffect offer = (TemptingOfferCreateTokensEffect) effect;
        createTokenEffectHandler.resolve(gameData, entry, offer.token());

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? apnapOpponents(gameData, entry.getControllerId())
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (opponents.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), entry.getSourcePermanentId(), entry.getXValue(),
                new TemptingOfferCreateTokensEffect(offer.token(), opponents,
                        entry.getControllerId()));
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
        TemptingOfferCreateTokensEffect effect, boolean accepted) {
        UUID controllerId = effect.abilityControllerId();
        StackEntry tokenEntry = sourceEntry(ability.sourceCard(), controllerId,
                ability.sourcePermanentId(), ability.xValue());

        if (accepted) {
            createTokenEffectHandler.resolveForController(gameData, tokenEntry, effect.token(), ability.controllerId());
            createTokenEffectHandler.resolveForController(gameData, tokenEntry, effect.token(), controllerId);
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), ability.sourcePermanentId(), ability.xValue(),
                    new TemptingOfferCreateTokensEffect(effect.token(), remaining,
                            controllerId));
        }
    }

    private void promptNext(GameData gameData, Card sourceCard, UUID sourcePermanentId,
                            int xValue, TemptingOfferCreateTokensEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                sourceCard.getName() + " - Create the offered tokens?",
                sourcePermanentId,
                null,
                xValue));
    }

    private StackEntry sourceEntry(Card sourceCard, UUID controllerId, UUID sourcePermanentId, Integer xValue) {
        return new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(),
                xValue == null ? 0 : xValue,
                sourcePermanentId);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
