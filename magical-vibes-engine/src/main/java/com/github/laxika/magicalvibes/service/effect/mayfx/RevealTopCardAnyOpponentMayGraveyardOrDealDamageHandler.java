package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffectHandler;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles each opponent's Sin Prodder choice. The first acceptance moves the revealed card to the
 * graveyard and deals damage immediately; only an all-decline sequence puts the card into hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardAnyOpponentMayGraveyardOrDealDamageHandler implements MayEffectHandlerBean {

    private final RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffectHandler effectHandler;
    private final DealDamageToPlayersEffectHandler dealDamageHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect) ability.effects().getFirst();
        UUID controllerId = effect.abilityControllerId();

        if (accepted) {
            Card topCard = takeRevealedTopCard(gameData, controllerId, effect.revealedCardId());
            if (topCard != null) {
                graveyardService.addCardToGraveyard(gameData, controllerId, topCard, Zone.LIBRARY);
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " puts ")
                        .card(topCard)
                        .text(" into its owner's graveyard.")
                        .build());
                dealDamage(gameData, ability, effect, player.getId());
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(player.getId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect next =
                    new RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect(
                            List.copyOf(remaining),
                            controllerId,
                            effect.sourcePermanentId(),
                            effect.revealedCardId(),
                            effect.manaValue());
            effectHandler.promptNext(gameData, ability.sourceCard(), next);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = takeRevealedTopCard(gameData, controllerId, effect.revealedCardId());
        if (topCard != null) {
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.builder()
                    .text("The revealed ")
                    .card(topCard)
                    .text(" is put into its owner's hand.")
                    .build());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private Card takeRevealedTopCard(GameData gameData, UUID controllerId, UUID revealedCardId) {
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty() || !deck.getFirst().getId().equals(revealedCardId)) {
            return null;
        }
        return deck.removeFirst();
    }

    private void dealDamage(GameData gameData, PendingMayAbility ability,
            RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect effect, UUID targetPlayerId) {
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                effect.manaValue(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                effect.abilityControllerId(),
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                targetPlayerId,
                effect.sourcePermanentId());
        dealDamageHandler.resolve(gameData, damageEntry, damage);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(targetPlayerId) + " chooses to put the revealed card into their graveyard; ",
                ability.sourceCard(), " deals " + effect.manaValue() + " damage to them."));
        log.info("Game {} - {} deals {} damage to {} after the Sin Prodder choice",
                gameData.id, ability.sourceCard().getName(), effect.manaValue(),
                gameData.playerIdToName.get(targetPlayerId));
    }
}
