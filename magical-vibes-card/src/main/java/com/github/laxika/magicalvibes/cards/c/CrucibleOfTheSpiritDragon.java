package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "167")
public class CrucibleOfTheSpiritDragon extends Card {

    public CrucibleOfTheSpiritDragon() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.STORAGE)),
                "{1}, {T}: Put a storage counter on this land."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.STORAGE),
                        new AwardAnyColorManaEffect(new XValue(), ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY,
                                CardSubtype.DRAGON, false)
                ),
                "{T}, Remove X storage counters from this land: Add X mana in any combination of colors. "
                        + "Spend this mana only to cast Dragon spells or activate abilities of Dragons."
        ));
    }
}
