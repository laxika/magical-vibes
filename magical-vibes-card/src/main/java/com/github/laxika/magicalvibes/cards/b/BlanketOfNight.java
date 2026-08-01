package com.github.laxika.magicalvibes.cards.b;

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
 * Blanket of Night — "Each land is a Swamp in addition to its other land types."
 *
 * <p>Additive layer-4 type grant to every land (any controller). Lands gain the intrinsic
 * "{T}: Add {B}" of the Swamp type the same way Prismatic Omen models basic-land mana abilities —
 * via an explicit {@link GrantActivatedAbilityEffect}, since additive subtype grants do not drive
 * the land-type override path used for type-replacing effects.
 */
@CardRegistration(set = "VIS", collectorNumber = "52")
public class BlanketOfNight extends Card {

    public BlanketOfNight() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.SWAMP, GrantScope.ALL_LANDS));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.BLACK),
                GrantScope.ALL_LANDS));
    }
}
