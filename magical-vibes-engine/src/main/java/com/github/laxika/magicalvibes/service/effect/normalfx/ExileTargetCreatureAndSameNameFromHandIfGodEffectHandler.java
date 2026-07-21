package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndSameNameFromHandIfGodEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureAndSameNameFromHandIfGodEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndSameNameFromHandIfGodEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Capture the creature's last-known characteristics before it leaves the battlefield.
        String targetName = target.getCard().getName();
        boolean wasGod = target.getCard().getSubtypes().contains(CardSubtype.GOD);
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

        permanentRemovalService.removePermanentToExile(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        log.info("Game {} - {} is exiled by {}",
                gameData.id, targetName, entry.getCard().getName());
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (!wasGod || controllerId == null) {
            return;
        }

        // It was a God: its controller reveals their hand and exiles all same-name cards from it.
        playerInteractionSupport.resolveRevealHand(gameData, controllerId);

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> toExile = new ArrayList<>();
        for (Card card : hand) {
            if (card.getName().equals(targetName)) {
                toExile.add(card);
            }
        }

        hand.removeAll(toExile);
        for (Card card : toExile) {
            gameData.addToExile(controllerId, card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " is exiled from hand."));
            log.info("Game {} - {} is exiled from hand by {}",
                    gameData.id, card.getName(), entry.getCard().getName());
        }
    }
}
