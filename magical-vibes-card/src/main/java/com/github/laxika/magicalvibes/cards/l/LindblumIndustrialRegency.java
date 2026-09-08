package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MageSiege;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "FIN", collectorNumber = "285")
@CardRegistration(set = "FIN", collectorNumber = "312")
public class LindblumIndustrialRegency extends Card {

    public LindblumIndustrialRegency() {
        setBackFaceCard(new MageSiege());
        addCastingOption(new AdventureCast("{2}{R}"));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }

    @Override
    public String getBackFaceClassName() {
        return "MageSiege";
    }
}
