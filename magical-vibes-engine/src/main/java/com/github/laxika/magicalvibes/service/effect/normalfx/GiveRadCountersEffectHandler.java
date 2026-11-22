package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link GiveRadCountersEffect}: evaluates the amount once (the defending player stands in for the amount's
 * target for {@code defendingPlayer()}), asks {@link SubjectResolutionService} for the players and gives each of them
 * the counters after counter replacements ({@link GameQueryService#replaceRadCounters}) through
 * {@link LifeSupport#applyRadCounters}, which honours "players can't get counters" and raises the "you put counters
 * on a player" event.
 */
@Component
@RequiredArgsConstructor
public class GiveRadCountersEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var rad = (GiveRadCountersEffect) effect;
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, rad.player());
        if (players.isEmpty()) {
            return;
        }
        AmountContext context = AmountContext.forStackEntry(entry, null);
        if (rad.player() instanceof PlayerSubject.DefendingPlayer) {
            context = context.withTargetPermanentId(players.getFirst());
        }
        int amount = amountEvaluationService.evaluate(gameData, rad.amount(), context);
        if (amount <= 0) {
            return;
        }
        String sourceName = entry.getCard() == null ? "rad counters" : FaceDownVisibility.sourceName(gameData, entry);
        UUID placingPlayerId = entry.getControllerId();
        for (UUID playerId : players) {
            int placed = gameQueryService.replaceRadCounters(gameData, playerId, amount, placingPlayerId);
            lifeSupport.applyRadCounters(gameData, playerId, placed, sourceName, placingPlayerId);
        }
    }
}
