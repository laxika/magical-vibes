package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.Builder;

/**
 * Unified effect for returning one or more cards from a graveyard to the controller's hand or
 * battlefield. Handles all graveyard-return patterns: choose one, return all, pre-targeted,
 * cross-graveyard search, aura attachment, equipment attachment to source, and life gain.
 *
 * <p>Resolution is dispatched to {@code GraveyardReturnResolutionService} via {@code @HandlesEffect},
 * which selects one of three paths:</p>
 * <ol>
 *   <li><b>Pre-targeted</b> — the card was targeted during casting or ability activation
 *       ({@link #targetGraveyard} is {@code true} and the stack entry has a graveyard target).
 *       Supports optional aura attachment via {@link #attachmentTarget}.</li>
 *   <li><b>Return all</b> — returns every matching card without player choice
 *       ({@link #returnAll} is {@code true}). Optionally restricted to cards that entered the
 *       graveyard from the battlefield this turn via {@link #thisTurnOnly}.</li>
 *   <li><b>Search and choose</b> — prompts the controller to pick a card at resolution time.
 *       Searches either the controller's graveyard or all graveyards based on {@link #source}.
 *       Supports optional equipment-to-source attachment via {@link #attachToSource}.</li>
 *   <li><b>Return at random</b> — returns a random matching card from the controller's graveyard
 *       without player choice ({@link #returnAtRandom} is {@code true}).</li>
 * </ol>
 *
 * <p>Use {@code ReturnCardFromGraveyardEffect.builder()} to construct instances. Only
 * {@code destination} is required; all other fields have sensible defaults ({@code source}
 * defaults to {@code CONTROLLERS_GRAVEYARD}, booleans to {@code false}, objects to {@code null}).</p>
 *
 * @param destination          where the returned card goes — {@code HAND} or {@code BATTLEFIELD}
 * @param filter               predicate restricting which graveyard cards qualify (e.g.
 *                             {@code CardTypePredicate(CREATURE)}, {@code CardSubtypePredicate(EQUIPMENT)});
 *                             {@code null} means any card
 * @param source               which graveyards to search — {@code CONTROLLERS_GRAVEYARD} (default),
 *                             {@code ALL_GRAVEYARDS}, or {@code OPPONENT_GRAVEYARD}
 * @param targetGraveyard      {@code true} if the card in the graveyard is targeted at cast/activation
 *                             time (enables {@link #canTargetGraveyard()}); {@code false} if the choice
 *                             happens at resolution time
 * @param returnAll            {@code true} to return all matching cards without player choice;
 *                             {@code false} to let the controller pick one
 * @param thisTurnOnly         {@code true} to restrict returned cards to those put into the graveyard
 *                             from the battlefield this turn (e.g. Faith's Reward, No Rest for the Wicked);
 *                             only meaningful when {@link #returnAll} is {@code true}
 * @param attachmentTarget     when non-null, the returned card (typically an Aura) is attached to a
 *                             permanent matching this predicate after entering the battlefield; the
 *                             controller chooses which permanent to attach to (e.g. Nomad Mythmaker)
 * @param gainLifeEqualToManaValue {@code true} if the controller gains life equal to the returned
 *                             card's mana value after it is returned (e.g. Razor Hippogriff)
 * @param attachToSource       {@code true} to offer the controller a second "you may" prompt to attach
 *                             the returned equipment to the source permanent that triggered this effect
 *                             (e.g. Auriok Survivors); the equipment enters the battlefield first, then
 *                             the controller may optionally attach it
 * @param grantHaste           {@code true} to grant haste to the permanent when it enters the battlefield
 *                             (e.g. Postmortem Lunge)
 * @param exileAtEndStep       {@code true} to schedule the permanent for exile at the beginning of the
 *                             next end step (e.g. Postmortem Lunge)
 * @param requiresManaValueEqualsX {@code true} to restrict targeting to cards whose mana value equals
 *                             the spell's X value (e.g. Postmortem Lunge)
 * @param grantColor           when non-null, permanently grants this color to the returned creature
 *                             "in addition to its other colors" (e.g. Rise from the Grave)
 * @param grantSubtype         when non-null, permanently grants this subtype to the returned creature
 *                             "in addition to its other types" (e.g. Rise from the Grave)
 * @param enterTapped          {@code true} if the returned permanent enters the battlefield tapped
 *                             (e.g. Reassembling Skeleton)
 * @param underOwnersControl   {@code true} to put each returned card onto the battlefield under
 *                             its owner's control (the player whose graveyard it was in) rather
 *                             than the spell controller's control (e.g. Open the Vaults)
 * @param returnAtRandom       {@code true} to return a random matching card instead of letting
 *                             the controller choose (e.g. Charmbreaker Devils)
 * @param randomCount          when {@link #returnAtRandom} is {@code true}, the number of random
 *                             cards to return (defaults to {@code 1}); capped at the number of
 *                             matching cards available (e.g. Make a Wish returns 2 at random)
 * @param choosePermanentType  {@code true} to prompt the controller to choose a permanent type
 *                             at resolution time, then return all cards of that type from the
 *                             graveyard (e.g. Creeping Renaissance); implies {@code returnAll}
 */
@Builder
public record ReturnCardFromGraveyardEffect(
        GraveyardChoiceDestination destination,
        CardPredicate filter,
        GraveyardSearchScope source,
        boolean targetGraveyard,
        boolean returnAll,
        boolean thisTurnOnly,
        PermanentPredicate attachmentTarget,
        boolean gainLifeEqualToManaValue,
        boolean attachToSource,
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean requiresManaValueEqualsX,
        CardColor grantColor,
        CardSubtype grantSubtype,
        boolean enterTapped,
        boolean underOwnersControl,
        boolean returnAtRandom,
        int randomCount,
        boolean choosePermanentType
) implements CardEffect {

    /**
     * Partial builder class providing default values. Booleans default to {@code false},
     * objects to {@code null}, and {@code source} to {@code CONTROLLERS_GRAVEYARD}.
     */
    public static class ReturnCardFromGraveyardEffectBuilder {

        private GraveyardSearchScope source = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
        private int randomCount = 1;
    }

    @Override
    public boolean canTargetGraveyard() {
        return targetGraveyard;
    }
}
