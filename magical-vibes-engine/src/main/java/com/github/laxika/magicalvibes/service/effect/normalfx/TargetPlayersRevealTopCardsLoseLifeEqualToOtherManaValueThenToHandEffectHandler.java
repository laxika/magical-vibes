package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Parker Luck: two target players (or the controller and a target player) each reveal the top card of their library,
 * each loses life equal to the mana value of the card the other revealed, then each puts the card they revealed into
 * their hand. The instructions are followed in that order (CR 608.2c): the revealed cards stay in their libraries
 * (CR 701.20a), the two losses are one simultaneous event, and only then do the cards go to hand.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: nothing changes before the losses, and the reveal can't change what a
 * Lich's Duel Mastery's sacrifice can be replaced with, so every such question (CR 616.1) is asked before anything
 * is revealed and the effect is resolved again with the answers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect revealEffect =
                (TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect) effect;
        List<UUID> declaredTargets = entry.getDeclaredTargetIds();
        if ((!revealEffect.controllerAndTarget() && declaredTargets.size() != 2)
                || (revealEffect.controllerAndTarget()
                && declaredTargets.size() != 1
                && entry.getTargetId() == null)) {
            return;
        }

        List<UUID> targets = revealEffect.controllerAndTarget()
                ? List.of(entry.getControllerId(), entry.getTargetId() != null
                ? entry.getTargetId() : declaredTargets.getFirst())
                : declaredTargets;
        boolean singleTargetPath = revealEffect.controllerAndTarget()
                && declaredTargets.isEmpty()
                && entry.getTargetId() != null;

        String sourceName = entry.getCard().getName();
        boolean[] legal = new boolean[targets.size()];
        Card[] revealed = new Card[2];
        for (int i = 0; i < targets.size(); i++) {
            legal[i] = revealEffect.controllerAndTarget()
                    ? i == 0 || singleTargetPath || entry.isTargetLegal(0)
                    : entry.isTargetLegal(i);
            List<Card> deck = gameData.playerDecks.get(targets.get(i));
            if (legal[i] && deck != null && !deck.isEmpty()) {
                revealed[i] = deck.getFirst();
            }
        }

        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (int i = 0; i < targets.size(); i++) {
            if (legal[i] && revealed[1 - i] != null) {
                losses.merge(targets.get(i), revealed[1 - i].getManaValue(), Integer::sum);
            }
        }
        lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, losses);

        for (int i = 0; i < targets.size(); i++) {
            if (!legal[i]) {
                continue;
            }
            String playerName = gameData.playerIdToName.get(targets.get(i));
            if (revealed[i] == null) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(revealed[i])
                    .text(" (mana value " + revealed[i].getManaValue() + ") from the top of their library ("
                            + sourceName + ").")
                    .build());
        }

        lifeSupport.applyLifeLosses(gameData, losses, sourceName);

        for (int i = 0; i < targets.size(); i++) {
            List<Card> deck = gameData.playerDecks.get(targets.get(i));
            if (revealed[i] != null && deck != null && deck.remove(revealed[i])) {
                gameData.addCardToHand(targets.get(i), revealed[i]);
            }
        }

        log.info("Game {} - target players reveal top cards and put them into hand via {}",
                gameData.id, sourceName);
    }
}
