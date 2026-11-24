package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;

import java.util.Set;
import java.util.UUID;

/**
 * The one rule for whether a player may search a library: read by the search paths (which log the prevention) and by
 * the choices that can't be made when searching is impossible (CR 608.2d).
 */
public final class LibrarySearchRestrictions {

    private LibrarySearchRestrictions() {}

    /** Why a library can't be searched: the restricting permanent, or {@code null} for a this-turn effect. */
    public record Restriction(Permanent source) {
    }

    /**
     * What stops {@code searcherId} from searching {@code libraryOwnerId}'s library when an effect controlled by
     * {@code causingControllerId} has them search it: {@code null} when nothing does. A permanent whose tax the searcher
     * paid this turn (Leonin Arbiter) stops nothing. Asking changes and logs nothing.
     */
    public static Restriction find(GameData gameData, GameQueryService gameQueryService, UUID searcherId,
                                   UUID libraryOwnerId, UUID causingControllerId) {
        // A floated copy (Shadow of Doubt) reads "you" as the player it is on.
        for (FloatingContinuousEffect floating
                : gameData.floatingEffectsOnPlayer(searcherId, CantSearchLibrariesEffect.class)) {
            UUID you = floating.affectedPlayerId() != null ? floating.affectedPlayerId() : floating.controllerId();
            if (SubjectResolutionService.namesPlayer(null, you, searcherId,
                    ((CantSearchLibrariesEffect) floating.effect()).players())) {
                return new Restriction(null);
            }
        }
        for (GameQueryService.ActiveStaticEffect<CardEffect> active
                : gameQueryService.activeStaticEffects(gameData, CardEffect.class)) {
            UUID restrictionControllerId = active.controllerId();
            CardEffect effect = active.effect();
            if (effect instanceof OpponentsCantSearchLibrariesEffect) {
                if (causingControllerId != null && libraryOwnerId.equals(causingControllerId)
                        && !restrictionControllerId.equals(causingControllerId)) {
                    return new Restriction(active.source());
                }
            } else if (effect instanceof CantSearchLibrariesEffect restriction && SubjectResolutionService.namesPlayer(
                    active.source(), restrictionControllerId, searcherId, restriction.players())) {
                Set<UUID> paid = gameData.paidSearchTaxPermanentIds.get(searcherId);
                if (!restriction.payableToIgnore() || paid == null || !paid.contains(active.source().getId())) {
                    return new Restriction(active.source());
                }
            }
        }
        return null;
    }
}
