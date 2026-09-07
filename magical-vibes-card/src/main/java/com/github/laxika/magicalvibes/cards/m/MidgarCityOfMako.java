package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.ReactorRaid;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "FIN", collectorNumber = "286")
@CardRegistration(set = "FIN", collectorNumber = "313")
public class MidgarCityOfMako extends Card {

    public MidgarCityOfMako() {
        setBackFaceCard(new ReactorRaid());
        addCastingOption(new AdventureCast("{2}{B}"));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }

    @Override
    public String getBackFaceClassName() {
        return "ReactorRaid";
    }
}
