package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromCreaturesItBlocksEffect;
import com.github.laxika.magicalvibes.model.effect.WallOnlyTargetingRestrictionEffect;

@CardRegistration(set = "CHR", collectorNumber = "41")
@CardRegistration(set = "LEG", collectorNumber = "128")
public class WallOfShadows extends Card {

    public WallOfShadows() {
        addStaticEffect(new PreventAllDamageToSelfFromCreaturesItBlocksEffect());
        addStaticEffect(new WallOnlyTargetingRestrictionEffect());
    }
}
