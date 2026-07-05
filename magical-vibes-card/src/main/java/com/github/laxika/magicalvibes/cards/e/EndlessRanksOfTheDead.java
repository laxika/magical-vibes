package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "99")
public class EndlessRanksOfTheDead extends Card {

    public EndlessRanksOfTheDead() {
        // At the beginning of your upkeep, create X 2/2 black Zombie creature tokens,
        // where X is half the number of Zombies you control, rounded down.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                new Divided(new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                        CountScope.CONTROLLER), 2),
                "Zombie", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()
        ));
    }
}
