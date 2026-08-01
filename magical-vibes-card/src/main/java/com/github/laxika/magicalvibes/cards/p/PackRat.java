package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "73")
public class PackRat extends Card {

    public PackRat() {
        // Pack Rat's power and toughness are each equal to the number of Rats you control (it counts itself).
        DynamicAmount rats = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.RAT), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(rats, rats));

        // {2}{B}, Discard a card: Create a token that's a copy of this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new DiscardCardTypeCost(null, null), new CreateTokenCopyOfSourceEffect()),
                "{2}{B}, Discard a card: Create a token that's a copy of this creature."));
    }
}
