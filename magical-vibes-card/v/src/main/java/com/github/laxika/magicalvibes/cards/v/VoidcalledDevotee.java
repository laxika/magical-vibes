package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ConjureCardInGraveyardEffect;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "13")
public class VoidcalledDevotee extends Card {

    public VoidcalledDevotee() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}"))));
        addEffect(EffectSlot.ON_ATTACK, new ConjureCardInGraveyardEffect(CantorOfTheRefrain::new));
    }
}
