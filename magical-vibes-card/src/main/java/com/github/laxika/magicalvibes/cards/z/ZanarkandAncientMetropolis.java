package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LastingFayth;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "FIN", collectorNumber = "293")
public class ZanarkandAncientMetropolis extends Card {

    public ZanarkandAncientMetropolis() {
        setBackFaceCard(new LastingFayth());
        addCastingOption(new AdventureCast("{4}{G}{G}"));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }

    @Override
    public String getBackFaceClassName() {
        return "LastingFayth";
    }
}
