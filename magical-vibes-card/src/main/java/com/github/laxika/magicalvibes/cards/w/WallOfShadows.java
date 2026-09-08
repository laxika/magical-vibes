package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromCreaturesItBlocksEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

@CardRegistration(set = "CHR", collectorNumber = "41")
public class WallOfShadows extends Card {

    public WallOfShadows() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageToSelfFromCreaturesItBlocksEffect());
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.wallOnlySpells());
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.wallOnlyAbilities());
    }
}
