package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "10")
public class CloudgoatRanger extends Card {

    public CloudgoatRanger() {
        // When this creature enters, create three 1/1 white Kithkin Soldier creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                3, "Kithkin Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER),
                Set.of(), Set.of()
        ));

        // Tap three untapped Kithkin you control: This creature gets +2/+0 and gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.KITHKIN)),
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                ),
                "Tap three untapped Kithkin you control: This creature gets +2/+0 and gains flying until end of turn."
        ));
    }
}
