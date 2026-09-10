package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Completes Sorin's optional top-card reveal, hand placement, and life loss. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueHandler
        implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect effect = ability.effects().stream()
                .filter(LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect.class::isInstance)
                .map(LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null
                || effect.stage() != LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect.Stage.MAY_REVEAL) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(ability.controllerId());
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " reveals ")
                    .card(topCard)
                    .text(" and puts it into their hand.")
                    .build());
            gameData.addCardToHand(ability.controllerId(), topCard);

            if (topCard.getManaValue() > 0) {
                lifeSupport.applyLifeLoss(gameData, ability.controllerId(), topCard.getManaValue(),
                        ability.sourceCard().getName());
            }
        } else if (!accepted) {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " chooses not to reveal the top card."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
