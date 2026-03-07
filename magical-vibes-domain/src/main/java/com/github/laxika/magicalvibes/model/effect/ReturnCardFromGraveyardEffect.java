package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

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
 * </ol>
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
 */
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
        boolean requiresManaValueEqualsX
) implements CardEffect {

    /**
     * Choose one matching card from the controller's graveyard at resolution time.
     *
     * @param destination where the returned card goes
     * @param filter      predicate restricting which cards qualify
     */
    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter) {
        this(destination, filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false, null, false, false, false, false, false);
    }

    /**
     * Choose one matching card from a specific graveyard scope at resolution time.
     *
     * @param destination where the returned card goes
     * @param filter      predicate restricting which cards qualify
     * @param source      which graveyards to search
     */
    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter,
                                         GraveyardSearchScope source) {
        this(destination, filter, source, false, false, false, null, false, false, false, false, false);
    }

    /**
     * Choose one matching card from the controller's graveyard, optionally targeting at cast time.
     *
     * @param destination     where the returned card goes
     * @param filter          predicate restricting which cards qualify
     * @param targetGraveyard {@code true} to target the graveyard card at cast/activation time
     */
    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter,
                                         boolean targetGraveyard) {
        this(destination, filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, targetGraveyard, false, false, null, false, false, false, false, false);
    }

    /**
     * Backward-compatible 8-parameter constructor (pre-{@code attachToSource}). Defaults
     * {@code attachToSource} to {@code false}.
     */
    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter,
                                         GraveyardSearchScope source, boolean targetGraveyard, boolean returnAll,
                                         boolean thisTurnOnly, PermanentPredicate attachmentTarget,
                                         boolean gainLifeEqualToManaValue) {
        this(destination, filter, source, targetGraveyard, returnAll, thisTurnOnly, attachmentTarget, gainLifeEqualToManaValue, false, false, false, false);
    }

    /**
     * Backward-compatible 9-parameter constructor (pre-{@code grantHaste/exileAtEndStep/requiresManaValueEqualsX}).
     */
    public ReturnCardFromGraveyardEffect(GraveyardChoiceDestination destination, CardPredicate filter,
                                         GraveyardSearchScope source, boolean targetGraveyard, boolean returnAll,
                                         boolean thisTurnOnly, PermanentPredicate attachmentTarget,
                                         boolean gainLifeEqualToManaValue, boolean attachToSource) {
        this(destination, filter, source, targetGraveyard, returnAll, thisTurnOnly, attachmentTarget, gainLifeEqualToManaValue, attachToSource, false, false, false);
    }

    @Override
    public boolean canTargetGraveyard() {
        return targetGraveyard;
    }
}
