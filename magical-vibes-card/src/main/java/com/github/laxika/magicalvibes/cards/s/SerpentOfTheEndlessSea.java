package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M10", collectorNumber = "70")
public class SerpentOfTheEndlessSea extends Card {

    public SerpentOfTheEndlessSea() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledSubtypeCountEffect(CardSubtype.ISLAND));
        addEffect(EffectSlot.STATIC, new CantAttackUnlessDefenderControlsMatchingPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                "an Island"
        ));
    }
}
