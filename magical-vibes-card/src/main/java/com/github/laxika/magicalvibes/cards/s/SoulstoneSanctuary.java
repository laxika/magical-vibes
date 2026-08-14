package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "FDN", collectorNumber = "133")
public class SoulstoneSanctuary extends Card {

    public SoulstoneSanctuary() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new AnimatePermanentsEffect(3, 3, List.of(), Set.of(Keyword.VIGILANCE, Keyword.CHANGELING))),
                "{4}: This land becomes a 3/3 creature with vigilance and all creature types. It's still a land."
        ));
    }
}
