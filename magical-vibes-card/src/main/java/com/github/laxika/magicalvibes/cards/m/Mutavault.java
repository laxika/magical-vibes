package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "148")
public class Mutavault extends Card {

    public Mutavault() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        // {1}: This land becomes a 2/2 creature with all creature types until end of turn. It's still a land.
        // "all creature types" == Changeling.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AnimatePermanentsEffect(2, 2, List.of(), Set.of(Keyword.CHANGELING))),
                "{1}: This land becomes a 2/2 creature with all creature types until end of turn. It's still a land."
        ));
    }
}
