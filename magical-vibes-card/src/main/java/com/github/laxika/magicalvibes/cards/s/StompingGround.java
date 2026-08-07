package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

@CardRegistration(set = "GTC", collectorNumber = "247")
public class StompingGround extends Card {

    public StompingGround() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(2));

        // {T}: Add {R} or {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
