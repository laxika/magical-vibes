package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionRacketEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Protection Racket's sequential upkeep process. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProtectionRacketEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionRacketEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ProtectionRacketEffect racket = (ProtectionRacketEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> opponents = racket.remainingOpponentIds().isEmpty()
                ? AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, controllerId)
                : new ArrayList<>(racket.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (opponents.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new ProtectionRacketEffect(
                opponents, controllerId, entry.getSourcePermanentId(), null, 0));
    }

    /** Presents the next opponent with the payment choice, or automatically puts the card into hand if payment is impossible. */
    public void promptNext(GameData gameData, Card sourceCard, ProtectionRacketEffect effect) {
        if (effect.remainingOpponentIds().isEmpty()) {
            return;
        }

        UUID opponentId = effect.remainingOpponentIds().getFirst();
        List<Card> library = gameData.playerDecks.get(effect.abilityControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(effect.abilityControllerId()) + " reveals ")
                .card(topCard)
                .text(" (mana value " + topCard.getManaValue() + ") from the top of their library ("
                        + sourceCard.getName() + ").")
                .build());

        ProtectionRacketEffect current = new ProtectionRacketEffect(
                effect.remainingOpponentIds(), effect.abilityControllerId(), effect.sourcePermanentId(),
                topCard.getId(), topCard.getManaValue());
        if (!canPayLife(gameData, opponentId, topCard.getManaValue())) {
            library.removeFirst();
            gameData.addCardToHand(effect.abilityControllerId(), topCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(effect.abilityControllerId()) + " puts the revealed ")
                    .card(topCard)
                    .text(" into their hand because " + gameData.playerIdToName.get(opponentId)
                            + " cannot pay its mana value in life.")
                    .build());
            advanceWithoutChoice(gameData, sourceCard, current);
            return;
        }

        String prompt = "Pay " + topCard.getManaValue() + " life to exile " + topCard.getName()
                + "? If you don't, put it into its owner's hand.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, opponentId, List.of(current), prompt, null, null, effect.sourcePermanentId()));
        playerInputService.processNextMayAbility(gameData);
        log.info("Game {} - offering {} the Protection Racket payment choice for {}",
                gameData.id, gameData.playerIdToName.get(opponentId), topCard.getName());
    }

    public boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return amount == 0 || (gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= amount);
    }

    private void advanceWithoutChoice(GameData gameData, Card sourceCard, ProtectionRacketEffect effect) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.removeFirst();
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new ProtectionRacketEffect(
                    remaining, effect.abilityControllerId(), effect.sourcePermanentId(), null, 0));
        }
    }
}
