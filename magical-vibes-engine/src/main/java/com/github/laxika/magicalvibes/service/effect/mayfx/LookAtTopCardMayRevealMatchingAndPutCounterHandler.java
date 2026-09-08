package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Completes the matching top-card reveal choice, leaving the card on top and applying the counter
 * to the source permanent when accepted.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingAndPutCounterHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingAndPutCounterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealMatchingAndPutCounterEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealMatchingAndPutCounterEffect)
                .map(e -> (LookAtTopCardMayRevealMatchingAndPutCounterEffect) e)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(ability.controllerId());
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.getFirst();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", topCard, " from the top of their library."));

            Permanent source = ability.sourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                StackEntry counterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        List.of(new PutCountersOnSelfEffect(effect.counterType())),
                        null,
                        ability.sourcePermanentId()
                );
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, counterEntry, source, effect.counterType(), 1);
            }
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " chooses not to reveal the top card."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
