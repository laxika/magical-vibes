package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "271")
public class SpellbaneCentaur extends Card {

    public SpellbaneCentaur() {
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.fromSourceColors(Set.of(CardColor.BLUE)));
    }
}
