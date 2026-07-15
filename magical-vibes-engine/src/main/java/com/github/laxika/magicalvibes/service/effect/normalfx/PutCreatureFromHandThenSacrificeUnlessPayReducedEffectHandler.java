package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandThenSacrificeUnlessPayReducedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Flash: "You may put a creature card from your hand onto the battlefield. If you do, sacrifice it
 * unless you pay its mana cost reduced by {2}." Prompts a declinable creature-from-hand choice; the
 * pay-or-sacrifice follow-up is handled after the creature enters (see {@code CardChoiceHandlerService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCreatureFromHandThenSacrificeUnlessPayReducedEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCreatureFromHandThenSacrificeUnlessPayReducedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCreatureFromHandThenSacrificeUnlessPayReducedEffect) effect;
        UUID playerId = entry.getControllerId();

        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> creatureIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).hasType(CardType.CREATURE)) {
                    creatureIndices.add(i);
                }
            }
        }

        if (creatureIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no creature cards in hand."));
            log.info("Game {} - {} has no creatures in hand for {}", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        playerInputService.beginCardChoiceSacrificeUnlessPayReduced(gameData, playerId, creatureIndices,
                "You may put a creature card from your hand onto the battlefield.", e.genericReduction());
    }
}
