package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandDrawPerMatchingCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The target opponent reveals their hand, then the caster draws one card for each card in it whose
 * subtypes or colors intersect the effect's filters. Each card is counted once (a card matching on
 * both a subtype and a color still yields a single draw). Used by Baleful Stare.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTargetHandDrawPerMatchingCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetHandDrawPerMatchingCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTargetHandDrawPerMatchingCardEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();

        // Reveal the opponent's hand to the caster (logs "looks at ... hand" + sends the reveal message).
        gameBroadcastService.revealOpponentHandToPlayer(gameData, casterId);

        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        long matches = hand.stream()
                .filter(card -> card.getSubtypes().stream().anyMatch(e.subtypes()::contains)
                        || card.getColors().stream().anyMatch(e.colors()::contains))
                .count();

        log.info("Game {} - {} draws {} card(s) from Baleful Stare-style reveal", gameData.id, casterId, matches);
        playerInteractionSupport.applyDrawCards(gameData, casterId, (int) matches);
    }
}
