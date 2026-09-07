package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayDrawOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Development's opponent choices and its fallback token. */
@Component
@RequiredArgsConstructor
public class AnyOpponentMayDrawOrCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMayDrawOrCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyOpponentMayDrawOrCreateTokenEffect) effect;
        UUID controllerId = e.abilityControllerId() != null
                ? e.abilityControllerId()
                : entry.getControllerId();
        List<UUID> opponents = e.remainingOpponentIds() == null
                ? AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, controllerId)
                : new ArrayList<>(e.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));

        if (opponents.isEmpty()) {
            createToken(gameData, entry.getCard(), controllerId, e.tokenEffect());
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyOpponentMayDrawOrCreateTokenEffect(
                e.tokenEffect(), opponents, controllerId, e.anyAccepted()));
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyOpponentMayDrawOrCreateTokenEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Have " + sourceCard.getName() + " have you draw a card? If no opponent does, "
                        + "its controller creates a 3/1 red Elemental creature token."));
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               AnyOpponentMayDrawOrCreateTokenEffect effect, boolean accepted) {
        boolean anyAccepted = effect.anyAccepted();
        if (accepted && !anyAccepted) {
            playerInteractionSupport.applyDrawCards(gameData, effect.abilityControllerId(), 1);
            anyAccepted = true;
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new AnyOpponentMayDrawOrCreateTokenEffect(
                    effect.tokenEffect(), remaining, effect.abilityControllerId(), anyAccepted));
            return;
        }

        if (!anyAccepted) {
            createToken(gameData, ability.sourceCard(), effect.abilityControllerId(), effect.tokenEffect());
        }
    }

    private void createToken(GameData gameData, Card sourceCard, UUID controllerId,
                             CreateTokenEffect tokenEffect) {
        StackEntry tokenEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(tokenEffect)));
        createTokenEffectHandler.resolveForController(gameData, tokenEntry, tokenEffect, controllerId);
    }
}
