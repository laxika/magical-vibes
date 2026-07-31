package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndAttachTriggeringAuraEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "2")
public class AjanisChosen extends Card {

    public AjanisChosen() {
        // Whenever an enchantment you control enters, create a 2/2 white Cat creature token.
        // If that enchantment is an Aura, you may attach it to the token.
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new CreateTokenAndAttachTriggeringAuraEffect(
                        new CreateTokenEffect("Cat", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.CAT), Set.of(), Set.of())));
    }
}
