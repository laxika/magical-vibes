package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "185")
public class SkybeastTracker extends Card {

    private static final CreateTokenEffect FOOD_TOKEN = CreateTokenEffect.ofArtifactToken(
            1,
            "Food",
            List.of(CardSubtype.FOOD),
            List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                    "{2}, {T}, Sacrifice this token: You gain 3 life."
            )));

    public SkybeastTracker() {
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(4))),
                FOOD_TOKEN);
    }
}
