package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "84")
public class DwarvenBloodboiler extends Card {

    public DwarvenBloodboiler() {
        // Tap an untapped Dwarf you control: Target creature gets +2/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.DWARF)),
                        new BoostTargetCreatureEffect(2, 0)
                ),
                "Tap an untapped Dwarf you control: Target creature gets +2/+0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
