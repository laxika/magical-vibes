package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NaturalBalanceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link NaturalBalanceEffect}. Land counts are read once, up front, so the two halves are
 * over disjoint player sets: players with six or more lands sacrifice down to five (they choose
 * which of their lands to sacrifice), players with four or fewer may search for up to five minus
 * their land count basic land cards. Both halves are queued in APNAP order (CR 101.4) and run
 * through {@link NaturalBalanceSupport}, searches first.
 */
@Component
@RequiredArgsConstructor
public class NaturalBalanceEffectHandler implements NormalEffectHandlerBean {

    private static final int TARGET_LANDS = 5;

    private final PredicateEvaluationService predicateEvaluationService;
    private final NaturalBalanceSupport naturalBalanceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NaturalBalanceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<LibrarySearchFollowUp.BasicLandsPick> searchers = new ArrayList<>();
        List<PendingForcedSacrifice> sacrificers = new ArrayList<>();

        for (UUID playerId : orderedApnap(gameData)) {
            List<Permanent> lands = lands(gameData, playerId);
            if (lands.size() > TARGET_LANDS) {
                List<UUID> landIds = lands.stream().map(Permanent::getId).toList();
                sacrificers.add(new PendingForcedSacrifice(playerId, lands.size() - TARGET_LANDS, landIds));
            } else if (lands.size() < TARGET_LANDS) {
                searchers.add(new LibrarySearchFollowUp.BasicLandsPick(playerId, TARGET_LANDS - lands.size()));
            }
        }

        naturalBalanceSupport.advance(gameData, LibrarySearchFollowUp.naturalBalance(searchers, sacrificers));
    }

    private List<Permanent> lands(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return List.of();
        }
        PermanentIsLandPredicate isLand = new PermanentIsLandPredicate();
        return battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, isLand))
                .toList();
    }

    /** Active player first, then every other player in seating order (CR 101.4 APNAP). */
    private List<UUID> orderedApnap(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> ordered = new ArrayList<>();
        if (gameData.orderedPlayerIds.contains(activePlayerId)) {
            ordered.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                ordered.add(playerId);
            }
        }
        return ordered;
    }
}
