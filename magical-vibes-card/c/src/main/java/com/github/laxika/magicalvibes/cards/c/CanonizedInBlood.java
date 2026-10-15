package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "96")
public class CanonizedInBlood extends Card {

    public CanonizedInBlood() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new DescendedThisTurn(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                1, "Vampire Demon", 4, 3, CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLACK),
                                List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
                                Set.of(Keyword.FLYING), Set.of())
                ),
                "{5}{B}{B}, Sacrifice this enchantment: Create a 4/3 white and black Vampire Demon creature token with flying."
        ));
    }
}
