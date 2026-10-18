package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "191")
public class SafewrightCavalry extends Card {

    private static final PermanentPredicate ELF = new PermanentHasSubtypePredicate(CardSubtype.ELF);

    public SafewrightCavalry() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new BoostTargetPermanentEffect(2, 2, ELF)),
                "{5}: Target Elf you control gets +2/+2 until end of turn.",
                new ControlledPermanentPredicateTargetFilter(
                        ELF,
                        "Target must be an Elf you control"
                )
        ));
    }
}
