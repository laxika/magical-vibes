package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;

@CardRegistration(set = "RAV", collectorNumber = "267")
@CardRegistration(set = "RVR", collectorNumber = "264")
public class PariahsShield extends Card {

    public PariahsShield() {
        addStaticEffect(new RedirectPlayerDamageToEnchantedCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
