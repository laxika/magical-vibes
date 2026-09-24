package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.WhammyBurnContinuationEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Shared reveal and continuation helpers for Whammy Burn. */
@Component
@RequiredArgsConstructor
public class WhammyBurnSupport {

    private final GameLogService gameLogService;

    public void start(GameData gameData, StackEntry entry) {
        List<CardSubtype> deck = new ArrayList<>(CardSubtype.basicLandTypes());
        Collections.shuffle(deck);
        revealNext(gameData, entry.getCard(), entry.getControllerId(), entry.getTargetId(), deck, 0);
    }

    public void continueAfterChoice(GameData gameData, PendingMayAbility ability, boolean accepted) {
        WhammyBurnContinuationEffect continuation =
                (WhammyBurnContinuationEffect) ability.effects().getFirst();
        if (!accepted) {
            queueDamage(gameData, continuation.revealedCount());
            return;
        }

        revealNext(gameData, ability.sourceCard(), ability.controllerId(), ability.targetCardId(),
                new ArrayList<>(continuation.remainingCards()), continuation.revealedCount());
    }

    private void revealNext(GameData gameData, Card sourceCard, UUID controllerId, UUID targetId,
                            List<CardSubtype> remainingCards, int revealedCount) {
        if (remainingCards.isEmpty()) {
            queueDamage(gameData, revealedCount);
            return;
        }

        CardSubtype revealed = remainingCards.remove(0);
        int newRevealedCount = revealedCount + 1;
        String playerName = gameData.playerIdToName.get(controllerId);
        String article = revealed == CardSubtype.ISLAND ? "an" : "a";
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + article + " "
                + revealed.getDisplayName() + " from the whammy deck for "
                + sourceCard.getName() + "."));

        if (revealed == CardSubtype.ISLAND) {
            return;
        }
        if (remainingCards.isEmpty()) {
            queueDamage(gameData, newRevealedCount);
            return;
        }

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(new WhammyBurnContinuationEffect(remainingCards, newRevealedCount)),
                sourceCard.getName() + " - " + revealed.getDisplayName()
                        + " revealed. Reveal another card from your whammy deck?",
                targetId));
    }

    private void queueDamage(GameData gameData, int damage) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Whammy Burn continuation has no parked spell resolution");
        }
        List<CardEffect> damageEffect = List.of(new DealDamageToAnyTargetEffect(damage));
        entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, damageEffect);
    }
}
