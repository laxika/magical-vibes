package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "53")
public class Benthicore extends Card {

    public Benthicore() {
        // When this creature enters, create two 1/1 blue Merfolk Wizard creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Merfolk Wizard", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.MERFOLK, CardSubtype.WIZARD),
                Set.of(), Set.of()
        ));

        // Tap two untapped Merfolk you control: Untap this creature. It gains shroud until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                        new UntapPermanentsEffect(TapUntapScope.SELF),
                        new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF)
                ),
                "Tap two untapped Merfolk you control: Untap this creature. It gains shroud until end of turn."
        ));
    }
}
