package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "XLN", collectorNumber = "284")
@CardRegistration(set = "AKH", collectorNumber = "287")
@CardRegistration(set = "HOU", collectorNumber = "204")
@CardRegistration(set = "M19", collectorNumber = "260")
@CardRegistration(set = "SOI", collectorNumber = "282")
public class WoodlandStream extends Card {

    public WoodlandStream() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
