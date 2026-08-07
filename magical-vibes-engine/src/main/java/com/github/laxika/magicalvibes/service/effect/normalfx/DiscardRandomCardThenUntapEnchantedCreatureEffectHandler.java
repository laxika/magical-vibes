package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardThenUntapEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Apathy: the enchanted creature's controller discards a card at random, and only if they did the
 * enchanted creature untaps. An empty hand means no discard and no untap.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardRandomCardThenUntapEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardRandomCardThenUntapEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The discarder is the enchanted creature's controller, baked onto the trigger's targetId by
        // StepTriggerService — not the Aura's controller, who resolves the entry.
        UUID discarderId = entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                ? entry.getTargetId()
                : entry.getControllerId();
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Aura";

        List<Card> hand = gameData.playerHands.get(discarderId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(discarderId) + " has no cards to discard."));
            return;
        }

        // The player chose to discard, so this is a self-inflicted discard.
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveRandomDiscardCards(gameData, discarderId, sourceName, 1);

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || aura.getAttachedTo() == null) {
            log.info("Game {} - {} untap fizzles: Aura no longer attached", gameData.id, sourceName);
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            log.info("Game {} - {} untap fizzles: enchanted creature left the battlefield", gameData.id, sourceName);
            return;
        }

        tapUntapSupport.untapPermanent(gameData, enchanted);
        gameLogService.append(gameData, GameLog.textCardText(sourceName + " untaps ", enchanted.getCard(), "."));
        log.info("Game {} - {} untaps {}", gameData.id, sourceName, enchanted.getCard().getName());
    }
}
