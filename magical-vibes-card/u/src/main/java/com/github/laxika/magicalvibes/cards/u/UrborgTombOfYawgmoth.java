package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;

/**
 * "Each land is a Swamp in addition to its other land types." The grant is additive (CR 305.6),
 * so affected lands keep their printed types and abilities and gain the Swamp mana ability on top.
 * Urborg is a land itself and so is a Swamp too, hence the including-self subtype scope. Its own
 * black mana comes from the intrinsic ability of the Swamp type it grants itself (CR 305.6), not from
 * a printed ability, so changing that type in its text changes its mana (CR 305.7).
 */
@CardRegistration(set = "M15", collectorNumber = "248")
@CardRegistration(set = "PLC", collectorNumber = "165")
@CardRegistration(set = "V12", collectorNumber = "13")
@CardRegistration(set = "UMA", collectorNumber = "254")
@CardRegistration(set = "TSR", collectorNumber = "287")
@CardRegistration(set = "PIO", collectorNumber = "118")
@CardRegistration(set = "SLZ", collectorNumber = "121")
@CardRegistration(set = "SLZ", collectorNumber = "242")
@CardRegistration(set = "SLZ", collectorNumber = "363")
@CardRegistration(set = "LTC", collectorNumber = "375")
public class UrborgTombOfYawgmoth extends Card {

    public UrborgTombOfYawgmoth() {
        addStaticEffect(new GrantSubtypeEffect(
                CardSubtype.SWAMP, GrantScope.ALL_LANDS_INCLUDING_SELF));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.BLACK), GrantScope.ALL_LANDS));
    }
}
