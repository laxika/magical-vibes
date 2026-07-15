package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerTopCardMayGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Eye Spy: the controller looks at the top card of target player's
 * library, then may put it into that player's graveyard. Pushes a may-ability to
 * the controller carrying the resolved target player id.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTargetPlayerTopCardMayGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTargetPlayerTopCardMayGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " looks at the top card of " + targetName + "'s library (" + sourceName + ")."));
        log.info("Game {} - {} looks at top of {}'s library: {} ({})",
                gameData.id, controllerName, targetName, topCard.getName(), sourceName);

        LookAtTargetPlayerTopCardMayGraveyardEffect typed = (LookAtTargetPlayerTopCardMayGraveyardEffect) effect;

        // Wand of Denial only lets you bin the card if it's a nonland card.
        if (typed.nonlandOnly() && topCard.hasType(CardType.LAND)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text("The top card is a land; it stays on top (" + sourceName + ")."));
            return;
        }

        // "you may pay N life" — only offer the choice if the controller can pay.
        if (typed.lifeCost() > 0 && gameData.getLife(controllerId) < typed.lifeCost()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " can't pay " + typed.lifeCost() + " life (" + sourceName + ")."));
            return;
        }

        String prompt = typed.lifeCost() > 0
                ? sourceName + " — Pay " + typed.lifeCost() + " life to put " + topCard.getName()
                        + " into " + targetName + "'s graveyard?"
                : sourceName + " — Put " + topCard.getName() + " into " + targetName + "'s graveyard?";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new LookAtTargetPlayerTopCardMayGraveyardEffect(
                        targetPlayerId, typed.nonlandOnly(), typed.lifeCost())),
                prompt,
                null,
                null,
                entry.getSourcePermanentId()
        ));
    }
}
