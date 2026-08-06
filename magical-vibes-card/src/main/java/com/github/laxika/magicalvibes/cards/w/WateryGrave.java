package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

@CardRegistration(set = "GTC", collectorNumber = "249")
public class WateryGrave extends Card {

    public WateryGrave() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(2));

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
