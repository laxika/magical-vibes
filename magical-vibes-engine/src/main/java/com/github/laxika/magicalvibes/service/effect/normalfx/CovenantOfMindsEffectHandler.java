package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CovenantOfMindsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Covenant of Minds: reveals the top three cards of the controller's library, then prompts the
 * targeted opponent (via the may-ability accept/decline system) to either put those cards into the
 * controller's hand or leave them. The revealed cards remain on top of the library while the
 * opponent decides; the choice is applied in
 * {@code MayPenaltyChoiceHandlerService#handleCovenantOfMindsChoice}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CovenantOfMindsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CovenantOfMindsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (opponentId == null) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        List<Card> deck = gameData.playerDecks.get(controllerId);
        int revealCount = deck == null ? 0 : Math.min(3, deck.size());

        // Reveal the top cards (they stay on top of the library while the opponent decides).
        if (revealCount == 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    controllerName + "'s library is empty — no cards are revealed."));
        } else {
            String revealed = deck.subList(0, revealCount).stream()
                    .map(Card::getName)
                    .collect(Collectors.joining(", "));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    controllerName + " reveals the top " + revealCount + " card(s) of their library: " + revealed + "."));
        }

        String prompt = "Choose for " + controllerName + " (Covenant of Minds) — Accept: put the revealed"
                + " cards into their hand. Decline: those cards go to their graveyard and they draw five cards.";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), opponentId,
                List.of(new CovenantOfMindsEffect()), prompt));
        playerInputService.processNextMayAbility(gameData);

        log.info("Game {} - Covenant of Minds: {} reveals {} card(s), {} chooses",
                gameData.id, controllerName, revealCount, opponentName);
    }
}
