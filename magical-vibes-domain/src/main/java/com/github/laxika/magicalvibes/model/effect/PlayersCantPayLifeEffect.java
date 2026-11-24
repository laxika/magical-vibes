package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: players can't pay life [or sacrifice matching permanents] to cast spells or activate abilities. Only
 * the costs of spells and abilities are restricted; life payments and sacrifices a resolving effect demands are not.
 * Paying 0 life is always allowed (CR 119.4b).
 *
 * <ul>
 *   <li>Karn's Sylex: "players can't pay life to cast spells or to activate abilities that aren't mana abilities":
 *       {@code ()}.</li>
 *   <li>Angel of Jubilation: "players can't pay life or sacrifice creatures to cast spells or activate abilities":
 *       {@code (true, new PermanentIsCreaturePredicate())}.</li>
 *   <li>Yasharn, Implacable Earth: "… or sacrifice nonland permanents …":
 *       {@code (true, new PermanentNotPredicate(new PermanentIsLandPredicate()))}; a land creature may still be
 *       sacrificed.</li>
 * </ul>
 *
 * <p>Read by {@code GameQueryService.canPayLifeForCosts}, {@code canSacrificeCreaturesForCosts} and
 * {@code canSacrificePermanentForCosts}, over the active static abilities (a face-down or ability-less source restricts
 * nothing).
 *
 * @param includingManaAbilities whether paying life for a mana ability is stopped too
 * @param sacrificeFilter        the permanents that can't be sacrificed for those costs; {@code null} for none
 */
public record PlayersCantPayLifeEffect(boolean includingManaAbilities, PermanentPredicate sacrificeFilter)
        implements CardEffect {

    /** Karn's Sylex: life only, mana abilities exempt. */
    public PlayersCantPayLifeEffect() {
        this(false, null);
    }
}
