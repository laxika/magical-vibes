package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealCardsChooseOneToDiscardEffect} (Thieving Sprite): the target reveals X cards
 * from their hand (X = number of the caster's permanents matching the effect's {@code countFilter}),
 * then the caster chooses one of those cards for the target to discard.
 *
 * <p>Delegates to the shared Blackmail/Noggin Whack reveal-and-discard flow
 * ({@link PlayerInteractionSupport#beginRevealCardsChooseDiscard}), which uses
 * {@code RevealCardsDiscardChoice} (AI-backed). When the target's hand is already ≤ X, every card is
 * revealed and the caster's discard pick begins immediately; otherwise the target first picks which
 * X cards to reveal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsChooseOneToDiscardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
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

        int revealCount = countMatchingPermanents(gameData, casterId, e);
        if (revealCount <= 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    casterName + " controls no matching permanents — no cards are revealed."));
            log.info("Game {} - {} controls 0 matching permanents; nothing revealed", gameData.id, casterName);
            return;
        }

        playerInteractionSupport.beginRevealCardsChooseDiscard(gameData, entry, revealCount, 1);
    }

    private int countMatchingPermanents(GameData gameData, UUID playerId, RevealCardsChooseOneToDiscardEffect e) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.countFilter())) {
                count++;
            }
        }
        return count;
    }
}
