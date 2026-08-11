package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealCardsChooseOneToDiscardEffect}: the target reveals X cards from their hand,
 * where X is the effect's evaluated count, then the caster chooses one of those cards for the
 * target to discard.
 *
 * <p>Delegates to the shared Blackmail/Noggin Whack reveal-and-discard flow
 * ({@link PlayerInteractionSupport#beginRevealCardsChooseDiscard}), which uses
 * {@code RevealCardsDiscardChoice} (AI-backed). When the target's hand is already less than or
 * equal to X, every card is revealed and the caster's discard pick begins immediately; otherwise
 * the target first picks which X cards to reveal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsChooseOneToDiscardEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCardsChooseOneToDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealCardsChooseOneToDiscardEffect) effect;

        UUID casterId = entry.getControllerId();
        String casterName = gameData.playerIdToName.get(casterId);

        int revealCount = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.count(), AmountContext.forStackEntry(entry, null)));
        if (revealCount <= 0) {
            gameLogService.append(gameData, GameLog.text(casterName + " reveals no cards."));
            log.info("Game {} - {} reveals 0 cards; nothing revealed", gameData.id, casterName);
            return;
        }

        playerInteractionSupport.beginRevealCardsChooseDiscard(gameData, entry, revealCount, 1);
    }
}
