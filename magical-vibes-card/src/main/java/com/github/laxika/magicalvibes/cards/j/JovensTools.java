package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "386")
@CardRegistration(set = "HML", collectorNumber = "108")
public class JovensTools extends Card {

    public JovensTools() {
        addActivatedAbility(new ActivatedAbility(true, "{4}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL), "Walls")),
                "{4}, {T}: Target creature can't be blocked this turn except by Walls.",
                TargetFilters.creature()));
    }
}
