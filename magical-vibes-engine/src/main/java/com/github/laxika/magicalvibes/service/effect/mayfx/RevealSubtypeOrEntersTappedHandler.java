package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "As it enters, you may reveal a [subtype] card; if you don't, it enters tapped."
 * (Lorwyn dual lands, e.g. Ancient Amphitheater). Declining taps the just-entered permanent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealSubtypeOrEntersTappedHandler implements MayEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealSubtypeOrEntersTappedEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealSubtypeOrEntersTappedEffect revealOrTapped = ability.effects().stream()
                .filter(e -> e instanceof RevealSubtypeOrEntersTappedEffect)
                .map(e -> (RevealSubtypeOrEntersTappedEffect) e)
                .findFirst().orElse(null);
        if (revealOrTapped != null) {
            if (accepted) {
                List<Card> hand = gameData.playerHands.get(ability.controllerId());
                Card revealed = hand == null ? null : hand.stream()
                        .filter(c -> c.getSubtypes().contains(revealOrTapped.subtype()))
                        .findFirst().orElse(null);
                String revealedName = revealed != null ? revealed.getName() : revealOrTapped.subtype().getDisplayName();
                gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " reveals "
                        + revealedName + " — " + ability.sourceCard().getName() + " enters untapped.");
                log.info("Game {} - {} reveals {} to keep {} untapped", gameData.id,
                        player.getUsername(), revealedName, ability.sourceCard().getName());
            } else {
                Permanent source = ability.sourcePermanentId() != null
                        ? gameQueryService.findPermanentById(gameData, ability.sourcePermanentId()) : null;
                if (source != null) {
                    source.tap();
                }
                gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " declines — "
                        + ability.sourceCard().getName() + " enters tapped.");
                log.info("Game {} - {} declines to reveal; {} enters tapped", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
