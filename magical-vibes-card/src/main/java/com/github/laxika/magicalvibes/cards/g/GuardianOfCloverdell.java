package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "216")
public class GuardianOfCloverdell extends Card {

    public GuardianOfCloverdell() {
        // When this creature enters, create three 1/1 white Kithkin Soldier creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                3, "Kithkin Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER),
                Set.of(), Set.of()
        ));

        // {G}, Sacrifice a Kithkin: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.KITHKIN)
                                )),
                                "Sacrifice a Kithkin",
                                false
                        ),
                        new GainLifeEffect(1)
                ),
                "{G}, Sacrifice a Kithkin: You gain 1 life."
        ));
    }
}
