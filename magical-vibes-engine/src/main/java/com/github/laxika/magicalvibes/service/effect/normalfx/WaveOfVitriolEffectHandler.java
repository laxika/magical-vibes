package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WaveOfVitriolEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Wave of Vitriol's simultaneous sacrifice and per-land search. */
@Component
@RequiredArgsConstructor
public class WaveOfVitriolEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentPredicate NONBASIC_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));
    private static final PermanentPredicate SACRIFICE_FILTER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsEnchantmentPredicate(),
            NONBASIC_LAND));

    private final DestructionSupport destructionSupport;
    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WaveOfVitriolEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> sacrificeIds = new ArrayList<>();
        Map<UUID, Integer> sacrificedNonbasicLands = new HashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }

            List<Permanent> matching = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, SACRIFICE_FILTER))
                    .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                    .toList();
            sacrificeIds.addAll(matching.stream().map(Permanent::getId).toList());

            int landCount = (int) matching.stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, NONBASIC_LAND))
                    .count();
            if (landCount > 0) {
                sacrificedNonbasicLands.put(playerId, landCount);
            }
        }

        destructionSupport.performSimultaneousSacrifice(gameData, sacrificeIds);

        List<LibrarySearchFollowUp.BasicLandsPick> searches = basicLandSearchQueueSupport.apnapOrder(gameData)
                .stream()
                .filter(sacrificedNonbasicLands::containsKey)
                .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(
                        playerId, sacrificedNonbasicLands.get(playerId), true))
                .toList();
        if (!searches.isEmpty()) {
            basicLandSearchQueueSupport.advance(
                    gameData, LibrarySearchFollowUp.basicLandSearches(searches, List.of(), true));
        }
    }
}
