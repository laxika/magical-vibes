package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;

/**
 * Blanket of Night — "Each land is a Swamp in addition to its other land types."
 *
 * <p>The layered Swamp subtype supplies the intrinsic black mana ability to each land.
 * A later effect replacing that land type also replaces its intrinsic mana ability.
 */
@CardRegistration(set = "VIS", collectorNumber = "52")
public class BlanketOfNight extends Card {

    public BlanketOfNight() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.SWAMP, GrantScope.ALL_LANDS));

    }
}
