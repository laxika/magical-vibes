package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

@CardRegistration(set = "RTR", collectorNumber = "241")
@CardRegistration(set = "RNA", collectorNumber = "251")
@CardRegistration(set = "DIS", collectorNumber = "174")
@CardRegistration(set = "ECL", collectorNumber = "265")
@CardRegistration(set = "ECL", collectorNumber = "347")
public class HallowedFountain extends Card {

    public HallowedFountain() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(2));

        // {T}: Add {W} or {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
