package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

@CardRegistration(set = "TSB", collectorNumber = "34")
public class VoidmageProdigy extends Card {

    public VoidmageProdigy() {
        addMorph("{U}");
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.WIZARD),
                                "Sacrifice a Wizard",
                                false),
                        new CounterSpellEffect()
                ),
                "{U}{U}, Sacrifice a Wizard: Counter target spell."
        ));
    }
}
