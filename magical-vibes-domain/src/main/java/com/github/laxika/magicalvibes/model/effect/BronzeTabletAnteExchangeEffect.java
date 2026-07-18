package com.github.laxika.magicalvibes.model.effect;

/**
 * Bronze Tablet's activated ante ability: "Exile this artifact and target nontoken permanent an
 * opponent owns. That player may pay {@code lifeCost} life. If they do, put this card into its
 * owner's graveyard. Otherwise, that player owns this card and you own the other exiled card."
 *
 * <p>Both Bronze Tablet and the targeted permanent are exiled unconditionally (before the pay
 * decision). The permanent's owner — "that player" — may then pay the life:
 * <ul>
 *   <li>pay → Bronze Tablet moves from exile to its owner's graveyard (the targeted permanent stays
 *       exiled);</li>
 *   <li>don't pay (or can't) → the ownership of both exiled cards is exchanged.</li>
 * </ul>
 *
 * <p>The oracle "that player owns this card and you own the other exiled card" is an ante concept:
 * the permanent, cross-game transfer of card ownership is outside a single game's scope and is not
 * modeled (mirroring {@link TempestEfreetAnteExchangeEffect}). Within one game this effect resolves
 * to the observable zone movements only — both cards are exiled, and on a pay the Tablet is moved to
 * its owner's graveyard; the {@code ownerId} stamped at game setup is frozen and left unchanged.
 *
 * <p>Targets the nontoken permanent — the ability declares the legal targets through a permanent
 * predicate target filter; {@link #targetSpec()} declares the harmful permanent category. The paying
 * player is derived at resolution as the owner of the targeted permanent.
 *
 * @param lifeCost how much life the targeted permanent's owner may pay to avoid the exchange (10)
 */
public record BronzeTabletAnteExchangeEffect(int lifeCost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
