package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect} (Goblin Welder): the player
 * sacrifices the artifact and returns the artifact card at the same time. The sacrifice is departed at once rather
 * than ending the step, because whether the return is allowed is decided as the event happens (a sacrificed
 * Grafdigger's Cage still stops it), which a paused step can't carry to its continuation. Inside the handler's rerun
 * window ({@link AsksDeathReplacementsFirst}) a CR 616.1 question stops and resolves it again before anything
 * changed.
 */
@Component
@RequiredArgsConstructor
public class SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DepartureService departureService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPermanentId = entry.getTargetId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetPermanentId == null || targetCardIds.size() != 1) {
            return;
        }

        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetPermanentId);
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardIds.getFirst());
        if (targetPermanent == null || targetCard == null
                || !gameQueryService.isArtifact(targetPermanent)
                || !targetCard.hasType(CardType.ARTIFACT)) {
            return;
        }

        UUID artifactControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (artifactControllerId == null || !artifactControllerId.equals(graveyardOwnerId)) {
            return;
        }

        boolean returnBlocked = graveyardReturnSupport.isCardBlockedFromEnteringFromZone(
                gameData, targetCard, Zone.GRAVEYARD);
        DepartureService.Outcome outcome = departureService.departAsStep(gameData, entry, null,
                DepartureBatch.of(List.of(DepartureIntent.sacrifice(targetPermanentId))));
        if (!(outcome instanceof DepartureService.Outcome.Done done) || !done.results().getFirst().sacrificed()) {
            return;
        }

        if (returnBlocked) {
            return;
        }
        returnHandler.resolveForController(gameData, entry,
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        1, false, false),
                graveyardOwnerId);
    }
}
