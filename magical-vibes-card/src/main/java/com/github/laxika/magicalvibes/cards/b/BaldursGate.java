package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "266")
public class BaldursGate extends Card {

    public BaldursGate() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: Add X mana of any one color, where X is the number of other Gates you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardAnyColorManaEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER, true))),
                "{2}, {T}: Add X mana of any one color, where X is the number of other Gates you control."
        ));
    }
}
