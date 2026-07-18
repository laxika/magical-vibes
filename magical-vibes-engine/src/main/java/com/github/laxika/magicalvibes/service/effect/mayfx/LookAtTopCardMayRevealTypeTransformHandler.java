package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Look at top card, may reveal to transform — e.g. Delver of Secrets.
 * Per ruling (2011-09-22): you may reveal even if it's not an instant or sorcery.
 * The card stays on top of your library. Transform only happens if revealed card matches.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealTypeTransformHandler implements MayEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealTypeTransformEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealTypeTransformEffect revealTypeTransform = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealTypeTransformEffect)
                .map(e -> (LookAtTopCardMayRevealTypeTransformEffect) e)
                .findFirst().orElse(null);
        if (revealTypeTransform != null) {
            if (accepted) {
                List<Card> deck = gameData.playerDecks.get(ability.controllerId());
                if (!deck.isEmpty()) {
                    Card topCard = deck.getFirst();
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " reveals " , topCard, " from the top of their library."));

                    // Transform only if the revealed card matches the required types
                    boolean matches = revealTypeTransform.cardTypes().contains(topCard.getType())
                            || topCard.getAdditionalTypes().stream().anyMatch(revealTypeTransform.cardTypes()::contains);
                    if (matches) {
                        Permanent self = ability.sourcePermanentId() != null
                                ? gameQueryService.findPermanentById(gameData, ability.sourcePermanentId()) : null;
                        if (self != null && !self.isTransformed()) {
                            Card backFace = self.getOriginalCard().getBackFaceCard();
                            if (backFace != null) {
                                String frontName = self.getCard().getName();
                                self.setCard(backFace);
                                self.setTransformed(true);
                                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(frontName + " transforms into " , backFace, "."));
                                log.info("Game {} - {} transforms into {} (revealed instant/sorcery)",
                                        gameData.id, frontName, backFace.getName());
                            }
                        }
                    } else {
                        log.info("Game {} - {} revealed {} but it's not a matching type, no transform",
                                gameData.id, player.getUsername(), topCard.getName());
                    }
                }
            } else {
                String logEntry = player.getUsername() + " chooses not to reveal.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} declines to reveal top card ({})", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
