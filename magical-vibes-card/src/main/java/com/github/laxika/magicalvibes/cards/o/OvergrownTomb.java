package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

@CardRegistration(set = "RTR", collectorNumber = "243")
@CardRegistration(set = "GRN", collectorNumber = "253")
@CardRegistration(set = "ECL", collectorNumber = "266")
@CardRegistration(set = "ECL", collectorNumber = "350")
@CardRegistration(set = "RAV", collectorNumber = "279")
public class OvergrownTomb extends Card {

    public OvergrownTomb() {
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(2));

        // {T}: Add {B} or {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
