package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.Builder;

/**
 * Unified effect for returning one or more cards from a graveyard to the controller's hand or
 * battlefield. Handles all graveyard-return patterns: choose one, return all, pre-targeted,
 * cross-graveyard search, aura attachment, equipment attachment to source, and life gain.
 *
 * <p>Resolution is handled by {@code ReturnCardFromGraveyardEffectHandler}, which selects one of
 * three paths:</p>
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
 * @param upTo                 {@code true} when the graveyard target is "up to one" (optional at cast time,
 *                             e.g. Yawgmoth's Vile Offering). Default {@code false} means the graveyard
 *                             target is mandatory (e.g. Raise Dead, Crawl from the Cellar) even when the
 *                             spell also has optional permanent target groups
 * @param returnAll            {@code true} to return all matching cards without player choice;
 *                             {@code false} to let the controller pick one
 * @param thisTurnOnly         {@code true} to restrict returned cards to <b>creature</b> cards put into the
 *                             graveyard from the battlefield this turn (e.g. Faith's Reward, No Rest for the
 *                             Wicked); uses the {@code creatureCardsPutIntoGraveyardFromBattlefieldThisTurn}
 *                             tracking; only meaningful when {@link #returnAll} is {@code true}
 * @param fromBattlefieldThisTurn {@code true} to restrict returned cards to <b>all</b> cards (any type) put
 *                             into the graveyard from the battlefield this turn (e.g. Twilight Shepherd);
 *                             uses the {@code cardsPutIntoGraveyardFromBattlefieldThisTurn} tracking; only
 *                             meaningful when {@link #returnAll} is {@code true}
 * @param fromAnywhereThisTurn {@code true} to restrict returned cards to those put into the graveyard
 *                             from any zone this turn (e.g. Garna, the Bloodflame); uses the
 *                             {@code cardsPutIntoGraveyardFromAnywhereThisTurn} tracking in GameData;
 *                             only meaningful when {@link #returnAll} is {@code true}
 * @param discardedOrCycledThisTurn {@code true} to restrict returned cards to those the controller cycled
 *                             or discarded this turn (e.g. Shadow of the Grave); uses the
 *                             {@code cardsDiscardedOrCycledThisTurn} tracking in GameData; only meaningful
 *                             when {@link #returnAll} is {@code true}
 * @param targetPutIntoGraveyardFromBattlefieldThisTurn {@code true} to restrict the <b>targeted</b>
 *                             graveyard card to a creature card that was put into that graveyard from the
 *                             battlefield this turn (e.g. Grim Return); reads the
 *                             {@code creatureCardsPutIntoGraveyardFromBattlefieldThisTurn} tracking of the
 *                             graveyard's owner and is only meaningful when {@link #targetGraveyard} is
 *                             {@code true}
 * @param attachmentTarget     when non-null, the returned card (typically an Aura) is attached to a
 *                             permanent matching this predicate after entering the battlefield; the
 *                             controller chooses which permanent to attach to (e.g. Nomad Mythmaker)
 * @param gainLifeEqualToManaValue {@code true} if the controller gains life equal to the returned
 *                             card's mana value after it is returned (e.g. Razor Hippogriff)
 * @param loseLifeEqualToManaValue {@code true} if the controller loses life equal to the returned
 *                             card's mana value after it is returned (e.g. Reanimate); only meaningful
 *                             on the pre-targeted path
 * @param attachToSource       {@code true} to attach the returned card to the source permanent. On the
 *                             search-and-choose path the controller gets a second "you may" prompt and the
 *                             equipment enters the battlefield first (e.g. Auriok Survivors). On the
 *                             pre-targeted battlefield path the attachment is mandatory and prompt-free —
 *                             the Aura enters already attached to the source, and the ability fizzles when
 *                             the source is gone or the Aura can't legally enchant it (Hakim, Loreweaver)
 * @param grantHaste           {@code true} to grant haste to the permanent when it enters the battlefield
 *                             (e.g. Postmortem Lunge)
 * @param exileAtEndStep       {@code true} to schedule the permanent for exile at the beginning of the
 *                             next end step (Unearth, Postmortem Lunge, Shallow Grave). Unearth's extra
 *                             "exile it instead if it would leave the battlefield" clause is the separate
 *                             {@link #exileIfLeavesBattlefield} flag — Shallow Grave has no such clause
 * @param sacrificeAtEndStep   {@code true} to schedule the permanent for sacrifice at the beginning of the
 *                             next end step (Apprentice Necromancer)
 * @param requiresManaValueEqualsX {@code true} to restrict targeting to cards whose mana value equals
 *                             the spell's X value (e.g. Postmortem Lunge)
 * @param requiresManaValueAtMostX {@code true} to restrict targeting to cards whose mana value is
 *                             less than or equal to the spell's X value (e.g. Profane Command)
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
 * @param exileSourceFromGraveyard {@code true} to exile the source card from the controller's
 *                             graveyard before performing the return (e.g. Moldgraf Monstrosity);
 *                             ensures the source cannot be one of the randomly returned cards
 * @param enterAttacking       {@code true} if the returned permanent enters the battlefield attacking
 *                             (e.g. Warcry Phoenix); typically combined with {@link #enterTapped}
 * @param maxManaValueEqualsLifeGainedThisTurn {@code true} to restrict targeting to graveyard cards
 *                             whose mana value is less than or equal to the amount of life the
 *                             controller gained this turn (e.g. Moseo, Vein's New Dean); the cap is
 *                             read from {@code GameData.getLifeGainedThisTurn} at target-selection time
 * @param enterWithMannequinCounter {@code true} to put a mannequin counter on the returned permanent as
 *                             it enters the battlefield (e.g. Makeshift Mannequin). While a permanent has
 *                             a mannequin counter it gains "When this creature becomes the target of a
 *                             spell or ability, sacrifice it" (enforced in
 *                             {@code TriggerCollectionService}); only meaningful for {@code BATTLEFIELD}
 * @param grantSourceHasteIfSubtype when non-null, the source permanent (the one whose effect returned the
 *                             card) gains haste until end of turn if the returned card has this subtype
 *                             (e.g. Warren Pilferers — "If that card is a Goblin card, this creature gains
 *                             haste until end of turn"); only meaningful for {@code HAND} search-and-choose
 * @param greatestPower        {@code true} to restrict the (mandatory) search-and-choose to the matching
 *                             card(s) in the controller's graveyard with the greatest power; a single
 *                             greatest-power card is returned without a real decision, ties let the
 *                             controller pick one and cannot be declined (e.g. Desecrator Hag)
 * @param topmost              {@code true} to return the topmost matching card of the controller's
 *                             (ordered) graveyard with no choice at all (e.g. Shallow Grave — "Return
 *                             the top creature card of your graveyard to the battlefield"); nothing
 *                             happens when no card matches
 * @param exileIfLeavesBattlefield {@code true} to set the permanent's exile-if-leaves-battlefield
 *                             replacement (e.g. Dreams of the Dead — "If the creature would leave the
 *                             battlefield, exile it instead of putting it anywhere else"; also Unearth's
 *                             CR 702.100 rider, where it pairs with {@link #exileAtEndStep})
 * @param plusOneCountersIfSubtype when non-null, {@link #plusOneCounterCount} +1/+1 counters are put on
 *                             the returned permanent only if the returned card has this subtype (e.g. Defy
 *                             Death — "If it's an Angel, put two +1/+1 counters on it"); when null,
 *                             {@link #plusOneCounterCount} alone is an unconditional rider (e.g.
 *                             Miraculous Recovery — "Put a +1/+1 counter on it"); only meaningful for
 *                             {@code BATTLEFIELD}
 * @param plusOneCountersIfCondition when non-null, {@link #plusOneCounterCount} +1/+1 counters are put on
 *                             the returned permanent only if this condition is met as the effect resolves
 *                             (e.g. Necromantic Summons' spell mastery — "If there are two or more instant
 *                             and/or sorcery cards in your graveyard, that creature enters with two
 *                             additional +1/+1 counters on it"); combines with
 *                             {@link #plusOneCountersIfSubtype} as an AND when both are set
 * @param plusOneCounterCount  number of +1/+1 counters placed after the return; gated by
 *                             {@link #plusOneCountersIfSubtype} / {@link #plusOneCountersIfCondition} when
 *                             those fields are non-null
 * @param grantCumulativeUpkeepCost when non-null, the returned permanent gains that cumulative upkeep
 *                             cost as a persistent {@code UPKEEP_TRIGGERED} ability (e.g. Dreams of the
 *                             Dead — "That creature gains Cumulative upkeep {2}.")
 * @param enterWithCounter     when non-null, put {@link #enterWithCounterCount} counters of that type on
 *                             the returned permanent after it enters (e.g. Bogardan Phoenix death counter);
 *                             only meaningful for {@code BATTLEFIELD}
 * @param enterWithCounterCount number of {@link #enterWithCounter} counters to place; ignored when
 *                             {@code enterWithCounter} is null (defaults to {@code 0})
 * @param linkToSource         {@code true} to record the reanimated permanent on the source permanent's
 *                             {@code chosenPermanentId} (Coffin Queen), so a later
 *                             {@link RemoveLinkedPermanentEffect} trigger can still name "that creature"
 *                             after the ability that put it there has finished; only meaningful on the
 *                             pre-targeted {@code BATTLEFIELD} path
 * @param battlefieldIfCreatureElseHand {@code true} to route each returned card per its type — creature
 *                             cards go to the battlefield, everything else goes to the controller's hand
 *                             (e.g. Deadbridge Chant); only honoured on the {@link #returnAtRandom} path,
 *                             where it overrides {@link #destination}
 */
@Builder
public record ReturnCardFromGraveyardEffect(
        GraveyardChoiceDestination destination,
        CardPredicate filter,
        GraveyardSearchScope source,
        boolean targetGraveyard,
        boolean upTo,
        boolean returnAll,
        boolean thisTurnOnly,
        boolean fromBattlefieldThisTurn,
        boolean fromAnywhereThisTurn,
        boolean discardedOrCycledThisTurn,
        boolean targetPutIntoGraveyardFromBattlefieldThisTurn,
        PermanentPredicate attachmentTarget,
        boolean gainLifeEqualToManaValue,
        boolean loseLifeEqualToManaValue,
        boolean attachToSource,
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean sacrificeAtEndStep,
        boolean requiresManaValueEqualsX,
        boolean requiresManaValueAtMostX,
        CardColor grantColor,
        CardSubtype grantSubtype,
        boolean enterTapped,
        boolean underOwnersControl,
        boolean returnAtRandom,
        int randomCount,
        boolean choosePermanentType,
        boolean exileSourceFromGraveyard,
        boolean enterAttacking,
        boolean maxManaValueEqualsLifeGainedThisTurn,
        boolean enterWithMannequinCounter,
        CardSubtype grantSourceHasteIfSubtype,
        boolean greatestPower,
        boolean topmost,
        boolean exileIfLeavesBattlefield,
        String grantCumulativeUpkeepCost,
        CardSubtype plusOneCountersIfSubtype,
        Condition plusOneCountersIfCondition,
        int plusOneCounterCount,
        CounterType enterWithCounter,
        int enterWithCounterCount,
        boolean linkToSource,
        boolean battlefieldIfCreatureElseHand
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
    public TargetSpec targetSpec() {
        // Only the targeted-graveyard variant participates in cast/activation-time targeting; the
        // resolution-time variants pick their card later. The declared scope is source(): it is the
        // one place the own/opponent/all narrowing lives, so the kept validator and every
        // enumeration path read the same value.
        return targetGraveyard ? TargetSpec.benign(TargetPredicates.graveyardCard(source)) : TargetSpec.NONE;
    }
}
