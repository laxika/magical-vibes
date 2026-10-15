package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "78")
@CardRegistration(set = "NCC", collectorNumber = "178")
public class ProsperousPartnership extends Card {

    public ProsperousPartnership() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(2, "Citizen", 1, 1,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentIsCreaturePredicate()),
                        CreateTokenEffect.ofTreasureToken(1)
                ),
                "Tap three untapped creatures you control: Create a Treasure token."
        ));
    }
}
