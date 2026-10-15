package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypesToSelfEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "255")
public class StoneworkPackbeast extends Card {

    public StoneworkPackbeast() {
        addEffect(EffectSlot.STATIC, new GrantSubtypesToSelfEffect(List.of(
                CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD)));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}: Add one mana of any color."));
    }
}
