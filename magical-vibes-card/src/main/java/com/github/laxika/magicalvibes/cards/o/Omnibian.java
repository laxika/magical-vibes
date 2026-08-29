package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "119")
public class Omnibian extends Card {

    public Omnibian() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}{G}{U}",
                List.of(
                        new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FROG),
                        new SetBasePowerToughnessEffect(3, 3)),
                "{T}: Target creature becomes a Frog with base power and toughness 3/3 until end of turn.",
                TargetFilters.creature()));
    }
}
