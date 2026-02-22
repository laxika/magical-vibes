package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "361")
public class TreetopVillage extends Card {

    public TreetopVillage() {
        setEntersTapped(true);
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        // {1}{G}: Treetop Village becomes a 3/3 green Ape creature with trample until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new AnimateLandEffect(3, 3, List.of(CardSubtype.APE), Set.of(Keyword.TRAMPLE), CardColor.GREEN)),
                false,
                "{1}{G}: Treetop Village becomes a 3/3 green Ape creature with trample until end of turn. It's still a land."
        ));
    }
}
