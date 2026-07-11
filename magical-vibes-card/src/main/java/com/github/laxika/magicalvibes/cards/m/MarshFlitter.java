package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "125")
public class MarshFlitter extends Card {

    public MarshFlitter() {
        // When this creature enters, create two 1/1 black Goblin Rogue creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Goblin Rogue", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE), Set.of(), Set.of()));

        // Sacrifice a Goblin: This creature has base power and toughness 3/3 until end of turn.
        // Marsh Flitter is a Faerie (not a Goblin), so it can only sacrifice the tokens.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                                )),
                                "Sacrifice a Goblin",
                                false
                        ),
                        new SetBasePowerToughnessEffect(3, 3, GrantScope.SELF)
                ),
                "Sacrifice a Goblin: This creature has base power and toughness 3/3 until end of turn."
        ));
    }
}
