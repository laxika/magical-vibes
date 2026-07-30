package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "M13", collectorNumber = "221")
public class CathedralOfWar extends Card {

    public CathedralOfWar() {
        // This land enters tapped. (Exalted is an auto-loaded keyword.)
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
