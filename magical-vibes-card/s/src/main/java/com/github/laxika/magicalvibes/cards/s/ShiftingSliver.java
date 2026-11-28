package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "52")
@CardRegistration(set = "SLD", collectorNumber = "625")
public class ShiftingSliver extends Card {

    public ShiftingSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new CanBeBlockedOnlyByFilterEffect(sliver, "Sliver creatures"),
                Subjects.allCreatures(),
                sliver));
    }
}
