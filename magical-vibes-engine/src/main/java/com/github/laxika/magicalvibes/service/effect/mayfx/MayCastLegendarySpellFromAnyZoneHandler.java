package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLegendarySpellFromAnyZoneEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Routes Kaya's single cross-zone free-cast choice to the appropriate zone-specific cast path. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastLegendarySpellFromAnyZoneHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastLegendarySpellFromAnyZoneEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastLegendarySpellFromAnyZoneEffect effect = ability.effects().stream()
                .filter(MayCastLegendarySpellFromAnyZoneEffect.class::isInstance)
                .map(MayCastLegendarySpellFromAnyZoneEffect.class::cast)
                .findFirst()
                .orElseThrow();
        Card card = ability.sourceCard();

        if (inHand(gameData, player, card)) {
            mayCastHandlerService.handleMayCastFromHandWithoutPaying(
                    gameData, player, accepted, ability,
                    MayCastLegendarySpellFromAnyZoneEffect.class, true, false);
            return;
        }

        if (inGraveyard(gameData, player, card)) {
            if (accepted) {
                removeRemainingOffers(gameData);
            }
            mayCastHandlerService.handlePlayFromGraveyardChoice(
                    gameData, player, accepted, ability,
                    new PlayTargetCardFromGraveyardWithoutPayingManaCostEffect(effect.filter()));
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
        if (exiled != null && player.getId().equals(exiled.ownerId())
                && (!exiled.faceDown() || gameData.foretoldCardIds.contains(card.getId()))) {
            if (accepted) {
                removeRemainingOffers(gameData);
                exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, card.getId());
            } else {
                decline(gameData, player, card, "exile");
            }
            return;
        }

        if (!accepted) {
            decline(gameData, player, card, "the available zones");
            return;
        }
        gameLogService.append(gameData, GameLog.cardThen(card, " is no longer available to cast."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private static boolean inHand(GameData gameData, Player player, Card card) {
        List<Card> hand = gameData.playerHands.get(player.getId());
        return hand != null && hand.stream()
                .anyMatch(candidate -> candidate.getId().equals(card.getId()));
    }

    private static boolean inGraveyard(GameData gameData, Player player, Card card) {
        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        return graveyard != null && graveyard.stream()
                .anyMatch(candidate -> candidate.getId().equals(card.getId()));
    }

    private static void removeRemainingOffers(GameData gameData) {
        gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                .anyMatch(MayCastLegendarySpellFromAnyZoneEffect.class::isInstance));
    }

    private void decline(GameData gameData, Player player, Card card, String zone) {
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " declines to cast ", card, " from " + zone + "."));
        log.info("Game {} - {} declines to cast {} from {}", gameData.id,
                player.getUsername(), card.getName(), zone);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
