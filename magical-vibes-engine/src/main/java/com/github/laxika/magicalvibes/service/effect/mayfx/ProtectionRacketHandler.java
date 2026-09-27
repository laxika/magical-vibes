package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionRacketEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ProtectionRacketEffectHandler;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes one opponent's Protection Racket payment choice. */
@Component
@RequiredArgsConstructor
public class ProtectionRacketHandler implements MayEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final LifeSupport lifeSupport;
    private final ProtectionRacketEffectHandler effectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionRacketEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ProtectionRacketEffect effect = (ProtectionRacketEffect) ability.effects().getFirst();
        UUID opponentId = ability.controllerId();
        List<Card> library = gameData.playerDecks.get(effect.abilityControllerId());
        Card topCard = library == null || library.isEmpty() ? null : library.getFirst();
        if (topCard != null && topCard.getId().equals(effect.revealedCardId())) {
            boolean paid = accepted && effectHandler.canPayLife(gameData, opponentId, effect.manaValue());
            library.removeFirst();
            if (paid) {
                lifeSupport.applyLifePayment(gameData, opponentId, effect.manaValue(), ability.sourceCard().getName());
                exileService.exileCard(gameData, effect.abilityControllerId(), topCard);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " pays " + effect.manaValue() + " life and exiles ")
                        .card(topCard)
                        .text(" from the top of its owner's library.")
                        .build());
            } else {
                gameData.addCardToHand(effect.abilityControllerId(), topCard);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " declines to pay and puts ")
                        .card(topCard)
                        .text(" into its owner's hand.")
                        .build());
            }
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(opponentId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, ability.sourceCard(), new ProtectionRacketEffect(
                    remaining, effect.abilityControllerId(), effect.sourcePermanentId(), null, 0));
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
