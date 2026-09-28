package com.github.laxika.magicalvibes.cards.y;

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
 * Yavimaya, Cradle of Growth — "Each land is a Forest in addition to its other land types."
 *
 * <p>The additive Forest subtype also grants the intrinsic green mana ability to every land.
 */
@CardRegistration(set = "AA3", collectorNumber = "25")
@CardRegistration(set = "MH2", collectorNumber = "261")
@CardRegistration(set = "LTC", collectorNumber = "377")
public class YavimayaCradleOfGrowth extends Card {

    public YavimayaCradleOfGrowth() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.FOREST, GrantScope.ALL_LANDS));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.GREEN),
                GrantScope.ALL_LANDS));
    }
}
