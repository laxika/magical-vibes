package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.KinshipEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kinship (Morningtide) — the top card shares a creature type; you may reveal it to resolve
 * the reveal effects against the source creature. The card stays on top of the library.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KinshipHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;
    private final KinshipEffectHandler kinshipEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KinshipEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        KinshipEffect kinship = ability.effects().stream()
                .filter(e -> e instanceof KinshipEffect)
                .map(e -> (KinshipEffect) e)
                .findFirst().orElse(null);
        if (kinship != null && kinship.lookStep()) {
            if (accepted) {
                kinshipEffectHandler.lookAtTopCard(gameData, ability.controllerId(), ability.sourceCard(),
                        ability.sourcePermanentId(), ability.sourcePermanentSnapshot(), kinship);
            } else {
                gameLogService.append(gameData, GameLog.text(player.getUsername()
                        + " chooses not to look at the top card of their library."));
            }
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }
        if (kinship != null) {
            if (accepted) {
                List<Card> deck = gameData.playerDecks.get(ability.controllerId());
                if (deck != null && !deck.isEmpty()) {
                    gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " reveals ", deck.getFirst(), " from the top of their library."));
                }
                // The revealed-card effects still happen if the source left the battlefield in response.
                if (!kinship.revealEffects().isEmpty()) {
                    StackEntry kinshipEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                            ability.sourceCard(), ability.controllerId(),
                            ability.sourceCard().getName() + " (Kinship)",
                            kinship.revealEffects(), 0, ability.sourcePermanentId());
                    effectResolutionService.resolveEffects(gameData, kinshipEntry);
                }
            } else {
                gameLogService.append(gameData, GameLog.text(player.getUsername() + " chooses not to reveal."));
                log.info("Game {} - {} declines to reveal top card ({})", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            }
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
        }
    }
}
