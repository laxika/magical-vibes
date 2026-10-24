package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect} (Arcum Dagsson):
 * the target's controller sacrifices it as the resolving step, then that player may search for a noncreature
 * artifact card (CR 608.2c). The search is offered even when the creature couldn't be sacrificed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (target == null || targetControllerId == null) {
            return;
        }

        if (gameQueryService.canEffectCauseSacrifice(gameData, targetControllerId, entry.getControllerId())) {
            stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, targetId, this);
            return;
        }
        offerSearch(gameData, entry, targetControllerId);
    }

    /** "That player may search …": the sacrificed creature's last controller. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        List<DepartureResult> departures = entry.getResolutionResults().departures();
        if (!departures.isEmpty() && departures.getFirst().lastKnownControllerId() != null) {
            offerSearch(gameData, entry, departures.getFirst().lastKnownControllerId());
        }
    }

    private void offerSearch(GameData gameData, StackEntry entry, UUID playerId) {
        CardAllOfPredicate noncreatureArtifact = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        SearchLibraryEffect search = new SearchLibraryEffect(
                noncreatureArtifact, LibrarySearchDestination.BATTLEFIELD);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                playerId,
                List.of(search),
                entry.getCard().getName() + " — Search your library for a noncreature artifact card?",
                null,
                null,
                entry.getSourcePermanentId()));

        log.info("Game {} - {} may search their library for a noncreature artifact", gameData.id,
                gameData.playerIdToName.get(playerId));
    }
}
