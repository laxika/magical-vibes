package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "122")
public class LysAlanaScarblade extends Card {

    public LysAlanaScarblade() {
        // {T}, Discard an Elf card: Target creature gets -X/-X until end of turn, where X is the number of Elves you control.
        Scaled minusElves = new Scaled(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER), -1);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardSubtypePredicate(CardSubtype.ELF), "Elf"),
                        new BoostTargetCreatureEffect(minusElves, minusElves)),
                "{T}, Discard an Elf card: Target creature gets -X/-X until end of turn, "
                        + "where X is the number of Elves you control."
        ));
    }
}
