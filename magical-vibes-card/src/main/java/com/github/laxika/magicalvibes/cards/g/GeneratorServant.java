package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardHasteGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "143")
public class GeneratorServant extends Card {

    public GeneratorServant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardHasteGrantingManaEffect(ManaColor.COLORLESS, 2)
                ),
                "{T}, Sacrifice this creature: Add {C}{C}. If that mana is spent on a creature spell, it gains haste until end of turn."
        ));
    }
}
