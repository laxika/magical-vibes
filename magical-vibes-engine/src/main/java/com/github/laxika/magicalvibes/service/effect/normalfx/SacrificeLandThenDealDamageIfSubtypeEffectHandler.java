package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeLandThenDealDamageIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a controller's land sacrifice and subtype-dependent damage rider. */
@Component
@RequiredArgsConstructor
public class SacrificeLandThenDealDamageIfSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtypeEffectHandler
            sacrificeThenDamageHandler;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeLandThenDealDamageIfSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeLandThenDealDamageIfSubtypeEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (!gameQueryService.canEffectCauseSacrifice(gameData, controllerId, controllerId)) {
            return;
        }

        List<Permanent> lands = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(land -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, land, new PermanentIsLandPredicate()))
                .filter(land -> !gameQueryService.cantBeSacrificed(gameData, land))
                .toList();
        if (lands.isEmpty()) {
            return;
        }

        if (lands.size() == 1) {
            sacrificeThenDamageHandler.sacrificeThenDamageIfSubtype(
                    gameData, entry, List.of(lands.getFirst().getId()), e.subtype(), e.damage());
            return;
        }

        sacrificeThenDamageHandler.beginNextChooser(
                gameData,
                List.of(new PendingForcedSacrifice(
                        controllerId, 1, lands.stream().map(Permanent::getId).toList())),
                List.of(),
                e.subtype(),
                e.damage(),
                entry);
    }
}
