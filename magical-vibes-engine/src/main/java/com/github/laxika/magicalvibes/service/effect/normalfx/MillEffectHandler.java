package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MillEffect}: the players {@link SubjectResolutionService} finds for its subject mill. A target,
 * triggering or chosen player ("target player mills cards equal to the number of cards in their hand") has the amount
 * evaluated for each milling player with that player as its target, and each mills in turn. Any other subject's
 * amount is evaluated once in the entry's context; when it names several players ("each opponent mills two cards")
 * they mill at once, as one event (CR 101.4), so a "one or more cards are milled" trigger sees one mill.
 */
@Component
@RequiredArgsConstructor
public class MillEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mill = (MillEffect) effect;

        // Source-relative amounts (e.g. CountersOnSource for Grindclock) use the live source
        // permanent when it is still on the battlefield, else the last-known snapshot.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, mill.player());
        if (millingPlayerIsAmountTarget(mill.player())) {
            for (UUID playerId : players) {
                graveyardService.resolveMillPlayer(gameData, playerId,
                        evaluateCount(gameData, entry, mill, source, playerId));
            }
            return;
        }
        if (players.isEmpty()) {
            return;
        }
        int count = evaluateCount(gameData, entry, mill, source, null);
        if (players.size() == 1) {
            graveyardService.resolveMillPlayer(gameData, players.getFirst(), count);
        } else {
            graveyardService.resolveMillPlayersSimultaneously(gameData, players, count);
        }
    }

    private static boolean millingPlayerIsAmountTarget(PlayerSubject player) {
        return player instanceof PlayerSubject.TargetPlayer
                || player instanceof PlayerSubject.TriggeringPlayer
                || player instanceof PlayerSubject.ChosenPlayer;
    }

    private int evaluateCount(GameData gameData, StackEntry entry, MillEffect mill,
                               Permanent source, UUID targetPlayerId) {
        // The milling player stands in for the target; the entry stays in the context, so an amount that reads
        // the trigger event ("that spell's mana value") finds it.
        AmountContext context = targetPlayerId == null
                ? AmountContext.forStackEntry(entry, source)
                : AmountContext.forStackEntry(entry, source).withTargetPermanentId(targetPlayerId);
        return Math.max(0, amountEvaluationService.evaluate(gameData, mill.count(), context));
    }
}
