package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;

/**
 * "Each land is a Swamp in addition to its other land types." The grant is additive (CR 305.6),
 * so affected lands keep their printed types and abilities and gain the Swamp mana ability on top.
 * Urborg is a land itself and so is a Swamp too, hence the including-self subtype scope. Its own
 * Swamp mana ability is modelled as a printed ability rather than a self-grant: a land-type setter
 * (Blood Moon) strips printed abilities in layer 4 together with the Swamp type, so the two go
 * away as one.
 */
@CardRegistration(set = "M15", collectorNumber = "248")
@CardRegistration(set = "PLC", collectorNumber = "165")
public class UrborgTombOfYawgmoth extends Card {

    public UrborgTombOfYawgmoth() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.SWAMP, GrantScope.ALL_LANDS_INCLUDING_SELF));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.BLACK), GrantScope.ALL_LANDS));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
