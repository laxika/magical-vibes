package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayCastOneWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "139")
@CardRegistration(set = "PIP", collectorNumber = "667")
@CardRegistration(set = "PIP", collectorNumber = "1058")
public class PerceptionBobblehead extends Card {

    public PerceptionBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {3}, {T}: Look at the top X cards of your library, where X is the number of Bobbleheads
        // you control. You may cast a spell with mana value 3 or less from among them without
        // paying its mana cost. Put the rest on the bottom of your library in a random order.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new LookAtTopCardsMayCastOneWithoutPayingManaCostEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.BOBBLEHEAD),
                                CountScope.CONTROLLER),
                        new Fixed(3))),
                "{3}, {T}: Look at the top X cards of your library, where X is the number of "
                        + "Bobbleheads you control. You may cast a spell with mana value 3 or less "
                        + "from among them without paying its mana cost. Put the rest on the bottom "
                        + "of your library in a random order."
        ));
    }
}
