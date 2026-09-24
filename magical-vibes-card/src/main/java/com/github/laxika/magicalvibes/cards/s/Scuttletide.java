package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "61")
public class Scuttletide extends Card {

    public Scuttletide() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect("Crab", 0, 3, CardColor.BLUE,
                                List.of(CardSubtype.CRAB), Set.of(), Set.of())),
                "{1}, Discard a card: Create a 0/3 blue Crab creature token."
        ));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(),
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.CRAB))));
    }
}
