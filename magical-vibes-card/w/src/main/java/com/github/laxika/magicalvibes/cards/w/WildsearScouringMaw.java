package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;

@CardRegistration(set = "BLC", collectorNumber = "8")
@CardRegistration(set = "BLC", collectorNumber = "44")
public class WildsearScouringMaw extends Card {

    public WildsearScouringMaw() {
        addStaticEffect(new GrantCastTriggerToSpellsEffect(
                SpellCastTriggerGrant.CASCADE_TO_ENCHANTMENT_FROM_HAND, new CascadeEffect()));
    }
}
