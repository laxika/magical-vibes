package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "134")
@CardRegistration(set = "PIP", collectorNumber = "662")
@CardRegistration(set = "PIP", collectorNumber = "1061")
public class IntelligenceBobblehead extends Card {

    public IntelligenceBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {5}, {T}: Draw X cards, where X is the number of Bobbleheads you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new DrawCardEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.BOBBLEHEAD),
                        CountScope.CONTROLLER))),
                "{5}, {T}: Draw X cards, where X is the number of Bobbleheads you control."
        ));
    }
}
